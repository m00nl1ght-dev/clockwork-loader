package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.interfaces.ComponentInterfaceType;
import dev.m00nl1ght.clockwork.util.CollectionUtil;
import dev.m00nl1ght.clockwork.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class TargetType<T extends ComponentTarget> {

    protected final String id;
    protected final int internalIdx;
    protected final Class<T> targetClass;
    protected final PluginContainer plugin;
    protected final List<TargetType<? extends T>> directSubtargets = new ArrayList<>();
    protected final ArrayList<ComponentType<?, T>> components = new ArrayList<>();

    private Primer<T> primer;
    protected int subtargetIdxFirst = -1, subtargetIdxLast = -1;

    private TargetType(TargetDefinition definition, PluginContainer plugin, Class<T> targetClass, int internalIdx) {
        this.plugin = plugin;
        this.targetClass = targetClass;
        this.id = definition.getId();
        this.internalIdx = internalIdx;
        this.primer = new Primer<>(this);
    }

    public abstract boolean canAcceptFrom(TargetType<?> other);

    public void checkCompatibility(EventType<?, ?> eventType) {
        if (eventType.getTargetType() == null) {
            final var msg = "Event type for [] is not registered";
            throw new IllegalArgumentException(LogUtil.format(msg, eventType.getEventClassType().getType().getTypeName()));
        } else if (!this.canAcceptFrom(eventType.getTargetType())) {
            final var msg = "Component target [] cannot use event type of component in different target []";
            throw new IllegalArgumentException(LogUtil.format(msg, id, eventType.getTargetType()));
        }
    }

    public void checkCompatibility(ComponentInterfaceType<?, ?> interfaceType) {
        if (interfaceType.getTargetType() == null) {
            final var msg = "Interface type [] is not registered";
            throw new IllegalArgumentException(LogUtil.format(msg, interfaceType.getInterfaceClass().getTypeName()));
        } else if (!this.canAcceptFrom(interfaceType.getTargetType())) {
            final var msg = "Component target [] cannot use interface type from different target []";
            throw new IllegalArgumentException(LogUtil.format(msg, id, interfaceType.getTargetType()));
        }
    }

    public void checkCompatibility(ComponentType<?, ?> componentType) {
        if (!this.canAcceptFrom(componentType.getTargetType())) {
            final var msg = "Component target [] cannot get component in different target []";
            throw new IllegalArgumentException(LogUtil.format(msg, id, componentType.getTargetType()));
        }
    }

    public PluginContainer getPlugin() {
        return plugin;
    }

    public String getId() {
        return id;
    }

    public Class<T> getTargetClass() {
        return targetClass;
    }

    public int getInternalIdx() {
        return internalIdx;
    }

    public final boolean isInitialised() {
        return primer == null;
    }

    public final Primer<T> getPrimer() {
        return primer;
    }

    public List<ComponentType<?, T>> getOwnComponentTypes() {
        return Collections.unmodifiableList(components);
    }

    public abstract List<ComponentType<?, ? super T>> getComponentTypes();

    public abstract TargetType<? super T> getParent();

    public abstract TargetType<? super T> getRoot();

    public abstract List<TargetType<? extends T>> getAllSubtargets();

    public List<TargetType<? extends T>> getDirectSubtargets() {
        if (!isInitialised()) throw new IllegalStateException();
        return Collections.unmodifiableList(directSubtargets);
    }

    public int getSubtargetIdxFirst() {
        return subtargetIdxFirst;
    }

    public int getSubtargetIdxLast() {
        return subtargetIdxLast;
    }

    protected abstract void init();

    static <T extends ComponentTarget> TargetType<T> create(TargetDefinition def, PluginContainer plugin, Class<T> targetClass, int idx) {
        if (def.getParent() == null) {
            checkSuperclasses(def, targetClass, Object.class, plugin.getClockworkCore());
            return new Root<>(def, plugin, targetClass, idx);
        } else {
            final var found = plugin.getClockworkCore().getTargetType(def.getParent()).orElseThrow();
            if (found.isInitialised()) throw new IllegalStateException();
            if (found.targetClass.isAssignableFrom(targetClass)) {
                checkSuperclasses(def, targetClass, found.targetClass, plugin.getClockworkCore());
                @SuppressWarnings("unchecked") final var parentType = (TargetType<? super T>) found;
                return new ForSubclass<>(def, parentType, plugin, targetClass, idx);
            } else {
                throw PluginLoadingException.invalidParentForTarget(def, found);
            }
        }
    }

    private static void checkSuperclasses(TargetDefinition def, Class<?> targetClass, Class<?> expected, ClockworkCore core) {
        var current = targetClass;
        while ((current = current.getSuperclass()) != null) {
            if (current == expected) return;
            final var found = core.getTargetTypeUncasted(current);
            if (found.isPresent()) {
                throw PluginLoadingException.illegalTargetSubclass(def, targetClass, found.get());
            }
        }
    }

    public static class Primer<T extends ComponentTarget> {

        private final TargetType<T> targetType;

        private Primer(TargetType<T> targetType) {
            this.targetType = targetType;
        }

        public synchronized <C> ComponentType<C, T> register(ComponentDefinition def, PluginContainer plugin, Class<C> compClass) {
            if (targetType.isInitialised()) throw new IllegalStateException();
            final var componentType = new ComponentType<>(def, plugin, compClass, targetType);
            targetType.components.add(componentType);
            return componentType;
        }

        synchronized void init() {
            if (targetType.primer == null) throw new IllegalStateException();
            targetType.components.trimToSize();
            targetType.init();
            targetType.primer = null;
        }

    }

    private static final class Root<T extends ComponentTarget> extends TargetType<T> {

        protected final ArrayList<TargetType<? extends T>> allSubtargets = new ArrayList<>();

        private Root(TargetDefinition definition, PluginContainer plugin, Class<T> targetClass, int internalIdx) {
            super(definition, plugin, targetClass, internalIdx);
        }

        @Override
        protected void init() {
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
            if (!isInitialised()) throw new IllegalStateException();
            return Collections.unmodifiableList(allSubtargets);
        }

        @Override
        public boolean canAcceptFrom(TargetType<?> other) {
            return other == this;
        }

    }

    private static final class ForSubclass<T extends ComponentTarget> extends TargetType<T> {

        private final TargetType<? super T> root;
        private final TargetType<? super T> parent;
        private final List<ComponentType<?, ? super T>> compoundList;

        private ForSubclass(TargetDefinition definition, TargetType<? super T> parent, PluginContainer plugin, Class<T> targetClass, int internalIdx) {
            super(definition, plugin, targetClass, internalIdx);
            this.compoundList = CollectionUtil.compoundList(parent.getComponentTypes(), components);
            this.root = parent.getRoot();
            this.parent = parent;
            this.parent.directSubtargets.add(this);
        }

        @Override
        protected void init() {
            if (parent.primer != null) throw new IllegalStateException();
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
            return (List<TargetType<? extends T>>) root.getAllSubtargets().subList(subtargetIdxFirst, subtargetIdxLast + 1);
        }

        @Override
        public boolean canAcceptFrom(TargetType<?> other) {
            return other == this || parent.canAcceptFrom(other);
        }

    }

}
