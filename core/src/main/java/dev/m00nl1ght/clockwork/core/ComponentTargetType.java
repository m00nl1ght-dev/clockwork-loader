package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.CollectionUtil;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.*;
import java.util.function.BiConsumer;

public abstract class ComponentTargetType<T extends ComponentTarget> {

    protected final String id;
    protected final Class<T> targetClass;
    protected final PluginContainer plugin;
    protected final ArrayList<ComponentType<?, T>> components = new ArrayList<>();
    protected Map<Class<?>, EventType<?, T>> eventTypes;
    protected EventDispatcher[] events;
    private Primer<T> primer;

    private ComponentTargetType(ComponentTargetDefinition definition, PluginContainer plugin, Class<T> targetClass) {
        this.plugin = plugin;
        this.targetClass = targetClass;
        this.id = definition.getId();
        this.primer = new Primer<>(this);
    }

    public abstract List<ComponentType<?, ? super T>> getRegisteredTypes();

    public abstract int getComponentCount();

    public abstract ComponentTargetType<? super T> getParent();

    public abstract ComponentTargetType<? super T> getRoot();

    @SuppressWarnings("unchecked")
    public <E> EventType<E, T> getEventType(Class<E> eventClass) {
        if (eventTypes == null) throw new IllegalStateException("event types not constructed yet");
        final var type = eventTypes.get(eventClass);
        return type == null ? new EventType.Dummy(this, eventClass) : (EventType<E, T>) type;
    }

    public <F> FunctionalSubtarget<T, F> getSubtarget(Class<F> type) {
        Preconditions.notNull(type, "type");
        return new FunctionalSubtarget<>(this, type);
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

    public final boolean isPrimed() {
        return events != null;
    }

    public final Primer getPrimer() {
        return primer;
    }

    @SuppressWarnings("unchecked")
    static <T extends ComponentTarget> ComponentTargetType<T> create(ComponentTargetDefinition def, PluginContainer plugin, Class<T> targetClass) {
        if (def.getParent() == null) {
            return new Root<>(def, plugin, targetClass);
        } else {
            final var found = plugin.getClockworkCore().getTargetType(def.getParent()).orElseThrow();
            if (found.targetClass.isAssignableFrom(targetClass)) {
                return new ForSubclass<>(def, (ComponentTargetType<? super T>) found, plugin, targetClass);
            } else {
                throw PluginLoadingException.invalidParentForTarget(def, found);
            }
        }
    }

    public static class Primer<T extends ComponentTarget> {

        private final ComponentTargetType<T> pre;
        private final EventDispatcherRegistry dispReg;
        private final Map<Class<?>, EventDispatcher<?, T>> events = new HashMap<>();

        private Primer(ComponentTargetType<T> pre) {
            this.dispReg = pre.plugin.getClockworkCore().getEventDispatcherRegistry();
            this.pre = pre;
        }

        @SuppressWarnings("unchecked")
        public synchronized <C, E> void registerListener(ComponentType<C, T> componentType, Class<E> eventClass, BiConsumer<C, E> consumer) {
            if (pre.isPrimed()) throw new IllegalStateException();
            final var disp = (EventDispatcher<E, T>) events.computeIfAbsent(eventClass, c -> dispReg.getDispatcherFor(eventClass, pre));
            disp.registerListener(componentType, consumer);
        }

        public synchronized <C> ComponentType<C, T> register(ComponentDefinition def, PluginContainer plugin, Class<C> compClass) {
            if (pre.isPrimed()) throw new IllegalStateException();
            final var componentType = new ComponentType<>(def, plugin, compClass, pre, pre.getComponentCount());
            pre.components.add(componentType);
            return componentType;
        }

        synchronized void fuse() {
            if (pre.primer == null) throw new IllegalStateException();
            if (pre.getParent() != null && pre.getParent().primer != null) throw new IllegalStateException();
            pre.primer = null;
            pre.events = new EventDispatcher[events.size()];
            pre.eventTypes = new HashMap<>(events.size());
            var i = 0; for (var evt : events.values()) {
                pre.eventTypes.put(evt.eventClass, new EventType<>(pre, evt.eventClass, i));
                pre.components.trimToSize();
                pre.events[i] = evt;
                i++;
            }
        }

    }

    private static final class Root<T extends ComponentTarget> extends ComponentTargetType<T> {

        protected final List<ComponentType<?, ? super T>> publicList = Collections.unmodifiableList(components);

        private Root(ComponentTargetDefinition definition, PluginContainer plugin, Class<T> targetClass) {
            super(definition, plugin, targetClass);
        }

        @Override
        public List<ComponentType<?, ? super T>> getRegisteredTypes() {
            return publicList;
        }

        @Override
        public int getComponentCount() {
            return publicList.size();
        }

        @Override
        public ComponentTargetType<? super T> getParent() {
            return null;
        }

        @Override
        public ComponentTargetType<? super T> getRoot() {
            return this;
        }

    }

    private static final class ForSubclass<T extends ComponentTarget> extends ComponentTargetType<T> {

        private final ComponentTargetType<? super T> root;
        private final ComponentTargetType<? super T> parent;
        private final List<ComponentType<?, ? super T>> compoundList;

        private ForSubclass(ComponentTargetDefinition definition, ComponentTargetType<? super T> parent, PluginContainer plugin, Class<T> targetClass) {
            super(definition, plugin, targetClass);
            this.compoundList = CollectionUtil.compoundList(parent.getRegisteredTypes(), components);
            this.root = parent.getRoot();
            this.parent = parent;
        }

        @Override
        public List<ComponentType<?, ? super T>> getRegisteredTypes() {
            return compoundList;
        }

        @Override
        public int getComponentCount() {
            return compoundList.size();
        }

        @Override
        public ComponentTargetType<? super T> getParent() {
            return parent;
        }

        @Override
        public ComponentTargetType<? super T> getRoot() {
            return root;
        }

    }

}
