package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventListener;
import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.interfaces.ComponentInterfaceType;
import dev.m00nl1ght.clockwork.util.CollectionUtil;
import dev.m00nl1ght.clockwork.util.LogUtil;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.*;

public abstract class TargetType<T extends ComponentTarget> {

    protected final String id;
    protected final int internalIdx;
    protected final Class<T> targetClass;
    protected final PluginContainer plugin;
    protected final List<TargetType<? extends T>> subtargets = new ArrayList<>();
    protected final ArrayList<ComponentType<?, T>> components = new ArrayList<>();
    protected final Map<Class<?>, EventType<?, T>> eventTypes = new HashMap<>();
    protected final Map<Class<?>, ComponentInterfaceType<?, T>> interfaceTypes = new HashMap<>();

    private Primer<T> primer;
    protected int subtargetIdxFirst = -1, subtargetIdxLast = -1;

    private TargetType(TargetDefinition definition, PluginContainer plugin, Class<T> targetClass, int internalIdx) {
        this.plugin = plugin;
        this.targetClass = targetClass;
        this.id = definition.getId();
        this.internalIdx = internalIdx;
        this.primer = new Primer<>(this);
    }

    public final synchronized void registerEventType(EventType<?, T> eventType) {
        Preconditions.notNull(eventType, "eventType");
        final var existing = getEventType(eventType.getEventClass());
        if (existing == eventType) return;
        if (existing != null) throw new IllegalArgumentException(LogUtil.format(
                "There is already an event type for [] registered to target []",
                "[]", eventType.getEventClass().getSimpleName(), id));
        eventTypes.put(eventType.getEventClass(), eventType);
        eventType.register(this);
    }

    @SuppressWarnings("unchecked")
    protected <E extends Event> EventType<E, ? super  T> getEventType(Class<E> eventClass) {
        return (EventType<E, ? super T>) eventTypes.get(eventClass);
    }

    public final synchronized void registerInterfaceType(ComponentInterfaceType<?, T> interfaceType) {
        Preconditions.notNull(interfaceType, "interfaceType");
        final var existing = getInterfaceType(interfaceType.getInterfaceClass());
        if (existing == interfaceType) return;
        if (existing != null) throw new IllegalArgumentException(LogUtil.format(
                "There is already an interface type for [] registered to target []",
                "[]", interfaceType.getInterfaceClass().getSimpleName(), id));
        interfaceTypes.put(interfaceType.getInterfaceClass(), interfaceType);
        interfaceType.register(this);
    }

    @SuppressWarnings("unchecked")
    protected <I> ComponentInterfaceType<I, ? super T> getInterfaceType(Class<I> interfaceClass) {
        return (ComponentInterfaceType<I, ? super T>) interfaceTypes.get(interfaceClass);
    }

    public abstract boolean canAcceptFrom(TargetType<?> other);

    public void checkCompatibility(EventType<?, ?> eventType) {
        if (eventType.getTargetType() == null) {
            final var msg = "Event type for [] is not registered";
            throw new IllegalArgumentException(LogUtil.format(msg, eventType.getEventClass().getSimpleName()));
        } else if (!this.canAcceptFrom(eventType.getTargetType())) {
            final var msg = "Component target [] cannot use event type of component in different target []";
            throw new IllegalArgumentException(LogUtil.format(msg, id, eventType.getTargetType()));
        }
    }

    public void checkCompatibility(ComponentInterfaceType<?, ?> interfaceType) {
        if (interfaceType.getTargetType() == null) {
            final var msg = "Interface type [] is not registered";
            throw new IllegalArgumentException(LogUtil.format(msg, interfaceType.getInterfaceClass().getSimpleName()));
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

    public List<TargetType<? extends T>> getSubtargets() {
        return Collections.unmodifiableList(subtargets);
    }

    public int getSubtargetIdxFirst() {
        return subtargetIdxFirst;
    }

    public int getSubtargetIdxLast() {
        return subtargetIdxLast;
    }

    public final boolean isInitialised() {
        return primer == null;
    }

    public final Primer<T> getPrimer() {
        return primer;
    }

    public abstract List<ComponentType<?, ? super T>> getComponentTypes();

    public abstract TargetType<? super T> getParent();

    public abstract TargetType<? super T> getRoot();

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

        public <E extends Event, C> void registerEventListener(EventListener<E, T, C> listener) {
            // TODO
        }

        public <I, C extends I> void registerComponentInterface(Class<I> interfaceClass, ComponentType<C, T> componentType) {
            // TODO
        }

        synchronized void init() {
            if (targetType.primer == null) throw new IllegalStateException();
            targetType.components.trimToSize();
            targetType.init();
            targetType.primer = null;
        }

    }

    private static final class Root<T extends ComponentTarget> extends TargetType<T> {

        protected final List<ComponentType<?, ? super T>> publicList = Collections.unmodifiableList(components);

        private Root(TargetDefinition definition, PluginContainer plugin, Class<T> targetClass, int internalIdx) {
            super(definition, plugin, targetClass, internalIdx);
        }

        @Override
        protected void init() {
            for (var i = 0; i < components.size(); i++) components.get(i).init(i);
            populateSubtargetIdxs(this, 0);
        }

        private int populateSubtargetIdxs(TargetType<?> pointer, int idx) {
            pointer.subtargetIdxFirst = idx; idx++;
            for (final var sub : pointer.subtargets) idx = populateSubtargetIdxs(sub, idx);
            pointer.subtargetIdxLast = idx - 1;
            return idx;
        }

        @Override
        public List<ComponentType<?, ? super T>> getComponentTypes() {
            return publicList;
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
            this.parent.subtargets.add(this);
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
        protected <E extends Event> EventType<E, ? super  T> getEventType(Class<E> eventClass) {
            final var own = super.getEventType(eventClass);
            return own != null ? own : parent.getEventType(eventClass);
        }

        @Override
        protected <I> ComponentInterfaceType<I, ? super T> getInterfaceType(Class<I> interfaceClass) {
            final var own = super.getInterfaceType(interfaceClass);
            return own != null ? own : parent.getInterfaceType(interfaceClass);
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
        public boolean canAcceptFrom(TargetType<?> other) {
            return other == this || parent.canAcceptFrom(other);
        }

    }

}
