package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.interfaces.ComponentInterfaceType;
import dev.m00nl1ght.clockwork.util.CollectionUtil;
import dev.m00nl1ght.clockwork.util.LogUtil;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class TargetType<T extends ComponentTarget> {

    protected final LoadedPlugin plugin;
    protected final TargetDescriptor descriptor;
    protected final Class<T> targetClass;
    protected final List<TargetType<? extends T>> directSubtargets = new ArrayList<>();
    protected final ArrayList<ComponentType<?, T>> components = new ArrayList<>();

    protected int subtargetIdxFirst = -1, subtargetIdxLast = -1;

    private TargetType(LoadedPlugin plugin, TargetDescriptor descriptor, Class<T> targetClass) {
        this.plugin = plugin;
        this.descriptor = descriptor;
        this.targetClass = targetClass;
    }

    public abstract boolean canAcceptFrom(TargetType<?> other);

    public void checkCompatibility(EventType<?, ?> eventType) {
        if (eventType.getTargetType() == null) {
            final var msg = "Event type for [] is not registered";
            throw new IllegalArgumentException(LogUtil.format(msg, eventType.getEventClassType().getType().getTypeName()));
        } else if (!this.canAcceptFrom(eventType.getTargetType())) {
            final var msg = "Component target [] cannot use event type of component in different target []";
            throw new IllegalArgumentException(LogUtil.format(msg, "[]", this, eventType.getTargetType()));
        }
    }

    public void checkCompatibility(ComponentInterfaceType<?, ?> interfaceType) {
        if (interfaceType.getTargetType() == null) {
            final var msg = "Interface type [] is not registered";
            throw new IllegalArgumentException(LogUtil.format(msg, interfaceType.getInterfaceClass().getTypeName()));
        } else if (!this.canAcceptFrom(interfaceType.getTargetType())) {
            final var msg = "Component target [] cannot use interface type from different target []";
            throw new IllegalArgumentException(LogUtil.format(msg, "[]", this, interfaceType.getTargetType()));
        }
    }

    public void checkCompatibility(ComponentType<?, ?> componentType) {
        if (!this.canAcceptFrom(componentType.getTargetType())) {
            final var msg = "Component target [] cannot get component in different target []";
            throw new IllegalArgumentException(LogUtil.format(msg, "[]", this, componentType.getTargetType()));
        }
    }

    public LoadedPlugin getPlugin() {
        return plugin;
    }

    public TargetDescriptor getDescriptor() {
        return descriptor;
    }

    public String getId() {
        return descriptor.getId();
    }

    public Class<T> getTargetClass() {
        return targetClass;
    }

    public List<ComponentType<?, T>> getOwnComponentTypes() {
        return Collections.unmodifiableList(components);
    }

    public abstract List<ComponentType<?, ? super T>> getComponentTypes();

    public abstract TargetType<? super T> getParent();

    public abstract TargetType<? super T> getRoot();

    public abstract List<TargetType<? extends T>> getAllSubtargets();

    public List<TargetType<? extends T>> getDirectSubtargets() {
        return Collections.unmodifiableList(directSubtargets);
    }

    public int getSubtargetIdxFirst() {
        return subtargetIdxFirst;
    }

    public int getSubtargetIdxLast() {
        return subtargetIdxLast;
    }

    abstract void init();

    <C> ComponentType<C, T> addComponent(ComponentDescriptor component, LoadedPlugin plugin, Class<C> compClass) {
        final var componentType = new ComponentType<>(plugin, component, compClass, this);
        components.add(componentType);
        return componentType;
    }

    static <T extends ComponentTarget> TargetType<T> create(LoadedPlugin plugin, TargetDescriptor descriptor, Class<T> targetClass) {
        Preconditions.notNull(descriptor, "descriptor");
        Preconditions.notNullAnd(plugin, o -> o.getId().equals(descriptor.getPlugin().getId()), "plugin");
        Preconditions.notNullAnd(targetClass, o -> o.getName().equals(descriptor.getTargetClass()), "targetClass");
        if (descriptor.getParent() == null) {
            checkSuperclasses(descriptor, targetClass, Object.class, plugin.getClockworkCore());
            return new Root<>(plugin, descriptor, targetClass);
        } else {
            final var found = plugin.getClockworkCore().getTargetType(descriptor.getParent()).orElseThrow();
            if (found.targetClass.isAssignableFrom(targetClass)) {
                checkSuperclasses(descriptor, targetClass, found.targetClass, plugin.getClockworkCore());
                @SuppressWarnings("unchecked") final var parentType = (TargetType<? super T>) found;
                return new Branch<>(plugin, descriptor, parentType, targetClass);
            } else {
                throw PluginLoadingException.invalidParentForTarget(descriptor, found);
            }
        }
    }

    private static void checkSuperclasses(TargetDescriptor descriptor, Class<?> targetClass, Class<?> expected, ClockworkCore core) {
        var current = targetClass;
        while ((current = current.getSuperclass()) != null) {
            if (current == expected) return;
            final var found = core.getTargetTypeUncasted(current);
            if (found.isPresent()) {
                throw PluginLoadingException.illegalTargetSubclass(descriptor, targetClass, found.get());
            }
        }
    }

    private static final class Root<T extends ComponentTarget> extends TargetType<T> {

        protected final ArrayList<TargetType<? extends T>> allSubtargets = new ArrayList<>();

        private Root(LoadedPlugin plugin, TargetDescriptor descriptor, Class<T> targetClass) {
            super(plugin, descriptor, targetClass);
        }

        @Override
        protected void init() {
            components.trimToSize();
            for (var i = 0; i < components.size(); i++) components.get(i).init(i);
            populateSubtargets(this);
            allSubtargets.trimToSize();
        }

        private void populateSubtargets(TargetType<? extends T> pointer) {
            pointer.subtargetIdxFirst = allSubtargets.size();
            allSubtargets.add(pointer);
            for (final var sub : pointer.directSubtargets) populateSubtargets(sub);
            pointer.subtargetIdxLast = allSubtargets.size() - 1;
        }

        @Override
        public List<ComponentType<?, ? super T>> getComponentTypes() {
            return Collections.unmodifiableList(components);
        }

        @Override
        public TargetType<? super T> getParent() {
            return null;
        }

        @Override
        public TargetType<? super T> getRoot() {
            return this;
        }

        @Override
        public List<TargetType<? extends T>> getAllSubtargets() {
            return Collections.unmodifiableList(allSubtargets);
        }

        @Override
        public boolean canAcceptFrom(TargetType<?> other) {
            return other == this;
        }

    }

    private static final class Branch<T extends ComponentTarget> extends TargetType<T> {

        private final TargetType<? super T> root;
        private final TargetType<? super T> parent;
        private final List<ComponentType<?, ? super T>> compoundList;

        private Branch(LoadedPlugin plugin, TargetDescriptor descriptor, TargetType<? super T> parent, Class<T> targetClass) {
            super(plugin, descriptor, targetClass);
            this.compoundList = CollectionUtil.compoundList(parent.getComponentTypes(), components);
            this.root = parent.getRoot();
            this.parent = parent;
            this.parent.directSubtargets.add(this);
        }

        @Override
        protected void init() {
            components.trimToSize();
            final var bIdx = parent.getComponentTypes().size();
            for (var i = 0; i < components.size(); i++) components.get(i).init(bIdx + i);
        }

        @Override
        public List<ComponentType<?, ? super T>> getComponentTypes() {
            return compoundList;
        }

        @Override
        public TargetType<? super T> getParent() {
            return parent;
        }

        @Override
        public TargetType<? super T> getRoot() {
            return root;
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<TargetType<? extends T>> getAllSubtargets() {
            final var sub = root.getAllSubtargets();
            return IntStream.rangeClosed(subtargetIdxFirst, subtargetIdxLast)
                    .mapToObj(i -> (TargetType<? extends T>) sub.get(i))
                    .collect(Collectors.toUnmodifiableList());
        }

        @Override
        public boolean canAcceptFrom(TargetType<?> other) {
            return other == this || parent.canAcceptFrom(other);
        }

    }

}
