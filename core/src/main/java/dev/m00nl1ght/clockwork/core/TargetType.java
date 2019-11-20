package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.CollectionUtil;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.*;
import java.util.function.BiConsumer;

public abstract class TargetType<T extends ComponentTarget<? super T>> {

    protected final String id;
    protected final Class<T> targetClass;
    protected final PluginContainer plugin;
    protected final ArrayList<ComponentType<?, T>> components = new ArrayList<>();
    protected Map<Class<?>, EventType<?, T>> eventTypes;
    protected EventDispatcher[] events;
    private Primer<T> primer;

    private TargetType(TargetDefinition definition, PluginContainer plugin, Class<T> targetClass, EventDispatcherFactory dispatcherFactory) {
        this.plugin = plugin;
        this.targetClass = targetClass;
        this.id = definition.getId();
        this.primer = new Primer<>(this, dispatcherFactory);
    }

    public abstract List<ComponentType<?, ? super T>> getRegisteredTypes();

    public abstract int getComponentCount();

    public abstract TargetType<? super T> getParent();

    public abstract TargetType<? super T> getRoot();

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

    public final boolean isInitialised() {
        return primer == null;
    }

    public final Primer getPrimer() {
        return primer;
    }

    protected void init(Map<Class<?>, EventDispatcher<?, T>> events) {
        if (primer == null) throw new IllegalStateException();
        this.components.trimToSize();
        primer = null;
    }

    @SuppressWarnings("unchecked")
    static <T extends ComponentTarget<? super T>> TargetType<T> create(TargetDefinition def, PluginContainer plugin, Class<T> targetClass, EventDispatcherFactory dispatcherFactory) {
        if (def.getParent() == null) {
            return new Root<>(def, plugin, targetClass, dispatcherFactory);
        } else {
            // WARNING: this line below breaks IntelliJ code analysis if a specific inspection is enabled
            // see https://youtrack.jetbrains.com/issue/IDEA-227288
            final TargetType<?> found = plugin.getClockworkCore().getTargetType(def.getParent()).orElseThrow();
            if (found.targetClass.isAssignableFrom(targetClass)) {
                //noinspection Convert2Diamond for whatever reason the compiler chokes on <>
                return new ForSubclass<T>(def, (TargetType<? super T>) found, plugin, targetClass, dispatcherFactory);
            } else {
                throw PluginLoadingException.invalidParentForTarget(def, found);
            }
        }
    }

    public static class Primer<T extends ComponentTarget<? super T>> {

        private final TargetType<T> pre;
        private final EventDispatcherFactory dispatcherFactory;
        private final Map<Class<?>, EventDispatcher<?, T>> events = new HashMap<>();

        private Primer(TargetType<T> pre, EventDispatcherFactory dispatcherFactory) {
            this.dispatcherFactory = dispatcherFactory;
            this.pre = pre;
        }

        @SuppressWarnings("unchecked")
        public synchronized <C, E> void registerListener(ComponentType<C, T> componentType, Class<E> eventClass, BiConsumer<C, E> consumer, EventFilter<E, T> filter) {
            if (pre.isInitialised()) throw new IllegalStateException();
            final var disp = (EventDispatcher<E, T>) events.computeIfAbsent(eventClass, c -> dispatcherFactory.build(pre, eventClass));
            disp.registerListener(componentType, consumer, filter);
        }

        public synchronized <C> ComponentType<C, T> register(ComponentDefinition def, PluginContainer plugin, Class<C> compClass) {
            if (pre.isInitialised()) throw new IllegalStateException();
            final var componentType = new ComponentType<>(def, plugin, compClass, pre);
            pre.components.add(componentType);
            return componentType;
        }

        synchronized void init() {
            pre.init(events);
        }

    }

    private static final class Root<T extends ComponentTarget<? super T>> extends TargetType<T> {

        protected final List<ComponentType<?, ? super T>> publicList = Collections.unmodifiableList(components);

        private Root(TargetDefinition definition, PluginContainer plugin, Class<T> targetClass, EventDispatcherFactory dispatcherFactory) {
            super(definition, plugin, targetClass, dispatcherFactory);
        }

        @Override
        protected void init(Map<Class<?>, EventDispatcher<?, T>> eventList) {
            super.init(eventList);

            for (var i = 0; i < components.size(); i++) components.get(i).init(i);

            this.events = new EventDispatcher[eventList.size()];
            this.eventTypes = new HashMap<>(eventList.size());

            var i = 0; for (var evt : eventList.values()) {
                this.eventTypes.put(evt.eventClass, new EventType<>(this, evt.eventClass, i));
                this.events[i] = evt; i++;
            }
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
        public TargetType<? super T> getParent() {
            return null;
        }

        @Override
        public TargetType<? super T> getRoot() {
            return this;
        }

    }

    @SuppressWarnings("unchecked")
    private static final class ForSubclass<T extends ComponentTarget<? super T>> extends TargetType<T> {

        private final TargetType<? super T> root;
        private final TargetType<? super T> parent;
        private final List<ComponentType<?, ? super T>> compoundList;

        private ForSubclass(TargetDefinition definition, TargetType<? super T> parent, PluginContainer plugin, Class<T> targetClass, EventDispatcherFactory dispatcherFactory) {
            super(definition, plugin, targetClass, dispatcherFactory);
            this.compoundList = CollectionUtil.compoundList(parent.getRegisteredTypes(), components);
            this.root = parent.getRoot();
            this.parent = parent;
        }

        @Override
        protected void init(Map<Class<?>, EventDispatcher<?, T>> eventList) {
            if (parent.primer != null) throw new IllegalStateException();
            super.init(eventList);

            final var bIdx = parent.getComponentCount();
            for (var i = 0; i < components.size(); i++) components.get(i).init(bIdx + i);

            final var ext = new EventDispatcher[parent.events.length];
            final var own = new ArrayList<EventDispatcher<?, T>>();

            for (var evt : eventList.values()) {
                // WARNING: this line below breaks IntelliJ code analysis if a specific inspection is enabled
                // see https://youtrack.jetbrains.com/issue/IDEA-227288
                final EventType<?, ? super T> parent_disp = parent.eventTypes.get(evt.eventClass);
                if (parent_disp == null) {
                    own.add(evt);
                } else {
                    final var idx = parent_disp.getInternalId();
                    evt.linkToParent(parent.events[idx]);
                    ext[idx] = evt;
                }
            }

            this.events = new EventDispatcher[ext.length + own.size()];
            this.eventTypes = new HashMap<>(events.length);

            for (var i = 0; i < ext.length; i++) {
                final var evt = ext[i];
                this.events[i] = evt == null ? parent.events[i] : evt;
                this.eventTypes.put(evt.eventClass, new EventType<>(this, evt.eventClass, i));
            }

            for (var i = 0; i < own.size(); i++) {
                final var idx = ext.length + i;
                final var evt = own.get(i);
                this.events[idx] = evt;
                this.eventTypes.put(evt.eventClass, new EventType<>(this, evt.eventClass, idx));
            }

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
        public TargetType<? super T> getParent() {
            return parent;
        }

        @Override
        public TargetType<? super T> getRoot() {
            return root;
        }

    }

}
