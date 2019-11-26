package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.CollectionUtil;
import dev.m00nl1ght.clockwork.util.LogUtil;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.*;
import java.util.function.BiConsumer;

public abstract class TargetType<T extends ComponentTarget> {

    protected final String id;
    protected final Class<T> targetClass;
    protected final PluginContainer plugin;
    protected final ArrayList<ComponentType<?, T>> components = new ArrayList<>();
    protected Map<Class<?>, EventType<?, T>> eventTypes;
    protected EventListener[][] eventListeners;
    private Primer<T> primer;

    private TargetType(TargetDefinition definition, PluginContainer plugin, Class<T> targetClass, EventListenerFactory dispatcherFactory) {
        this.plugin = plugin;
        this.targetClass = targetClass;
        this.id = definition.getId();
        this.primer = new Primer<>(this, dispatcherFactory);
    }

    public abstract List<ComponentType<?, ? super T>> getRegisteredTypes();

    public abstract <E> EventType<E, T> getEventTypeInternal(Class<E> eventClass);

    public abstract int getComponentCount();

    public abstract TargetType<? super T> getParent();

    public abstract TargetType<? super T> getRoot();

    public abstract boolean canAcceptFrom(TargetType<?> other);

    public <E> EventType<E, T> getEventType(Class<E> eventClass) {
        if (eventTypes == null) throw new IllegalStateException();
        final var type = getEventTypeInternal(eventClass);
        return type == null ? new EventType.Dummy<>(eventClass) : type;
    }

    @SuppressWarnings("unchecked")
    <E, R extends ComponentTarget> void post(EventType<E, R> eventType, R object, E event) {
        final var listeners = eventListeners[eventType.internalId];
        for (var listener : listeners) {
            try {
                listener.accept(object, event);
            } catch (ExceptionInPlugin e) {
                throw e;
            } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
                this.checkCompatibility(object);
                throw e;
            } catch (Throwable t) {
                if (listener instanceof EventListener.Base) {
                    final var base = (EventListener.Base) listener;
                    throw ExceptionInPlugin.inEventHandler(base.getComponentType(), event, object, t);
                } else {
                    throw t;
                }
            }
        }
    }

    void checkCompatibility(ComponentTarget object) {
        if (!this.canAcceptFrom(object.getTargetType())) {
            final var msg = "Component target [] cannot post event to component in different target []";
            throw new IllegalArgumentException(LogUtil.format(msg, id, object.getTargetType()));
        }
    }

    public <F> FunctionalSubtarget<T, F> getSubtarget(Class<F> type) {
        Preconditions.notNull(type, "type");
        if (primer != null) throw new IllegalStateException();
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

    public final Primer<T> getPrimer() {
        return primer;
    }

    abstract void init(Map<Class<?>, List<EventListener<?, T>>> events);

    @SuppressWarnings("unchecked")
    void rebuildEventListeners(EventListenerFactory listenerFactory) {
        for (var evt : eventTypes.values()) {
            final var listeners = eventListeners[evt.internalId];
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] instanceof EventListener.Base) {
                    final var base = (EventListener.Base) listeners[i];
                    listeners[i] = rebuildListener(base, evt.eventClass, listenerFactory);
                }
            }
        }
    }

    <C, E> EventListener<E, T> rebuildListener(EventListener.Base<C, E, T> listener, Class<E> eventClass, EventListenerFactory factory) {
        final var comp = listener.getComponentType();
        final var consumer = listener.getConsumer();
        final var filter = listener.getFilter();
        return factory.build(comp, eventClass, consumer, filter);
    }

    void collectEventTypes(Collection<EventType<?, ?>> collection) {
        collection.addAll(eventTypes.values());
    }

    @SuppressWarnings("unchecked")
    static <T extends ComponentTarget> TargetType<T> create(TargetDefinition def, PluginContainer plugin, Class<T> targetClass, EventListenerFactory dispatcherFactory) {
        if (def.getParent() == null) {
            return new Root<>(def, plugin, targetClass, dispatcherFactory);
        } else {
            final var found = plugin.getClockworkCore().getTargetType(def.getParent()).orElseThrow();
            if (found.targetClass.isAssignableFrom(targetClass)) {
                return new ForSubclass<>(def, (TargetType<? super T>) found, plugin, targetClass, dispatcherFactory);
            } else {
                throw PluginLoadingException.invalidParentForTarget(def, found);
            }
        }
    }

    public static class Primer<T extends ComponentTarget> {

        private final TargetType<T> pre;
        private final EventListenerFactory listenerFactory;
        private final Map<Class<?>, List<EventListener<?, T>>> events = new HashMap<>();

        private Primer(TargetType<T> pre, EventListenerFactory listenerFactory) {
            this.listenerFactory = listenerFactory;
            this.pre = pre;
        }

        public synchronized <C, E> void registerListener(ComponentType<C, T> componentType, Class<E> eventClass, BiConsumer<C, E> consumer, EventFilter<E, T> filter) {
            if (pre.isInitialised()) throw new IllegalStateException();
            final var list = events.computeIfAbsent(eventClass, c -> new ArrayList<>(5));
            list.add(listenerFactory.build(componentType, eventClass, consumer, filter));
        }

        public synchronized <C> ComponentType<C, T> register(ComponentDefinition def, PluginContainer plugin, Class<C> compClass) {
            if (pre.isInitialised()) throw new IllegalStateException();
            final var componentType = new ComponentType<>(def, plugin, compClass, pre);
            pre.components.add(componentType);
            return componentType;
        }

        synchronized void init() {
            if (pre.primer == null) throw new IllegalStateException();
            pre.components.trimToSize();
            pre.init(events);
            pre.primer = null;
        }

    }

    private static final class Root<T extends ComponentTarget> extends TargetType<T> {

        protected final List<ComponentType<?, ? super T>> publicList = Collections.unmodifiableList(components);

        private Root(TargetDefinition definition, PluginContainer plugin, Class<T> targetClass, EventListenerFactory dispatcherFactory) {
            super(definition, plugin, targetClass, dispatcherFactory);
        }

        @Override
        void init(Map<Class<?>, List<EventListener<?, T>>> events) {
            for (var i = 0; i < components.size(); i++) components.get(i).init(i);

            this.eventListeners = new EventListener[events.size()][];
            this.eventTypes = new HashMap<>(events.size());

            var i = 0;
            for (var entry : events.entrySet()) {
                this.eventTypes.put(entry.getKey(), new EventType<>(entry.getKey(), i));
                this.eventListeners[i] = entry.getValue().toArray(EventListener[]::new);
                i++;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <E> EventType<E, T> getEventTypeInternal(Class<E> eventClass) {
            return (EventType<E, T>) eventTypes.get(eventClass);
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

        @Override
        public boolean canAcceptFrom(TargetType<?> other) {
            return other == this;
        }

    }

    @SuppressWarnings("unchecked")
    private static final class ForSubclass<T extends ComponentTarget> extends TargetType<T> {

        private final TargetType<? super T> root;
        private final TargetType<? super T> parent;
        private final List<ComponentType<?, ? super T>> compoundList;

        private ForSubclass(TargetDefinition definition, TargetType<? super T> parent, PluginContainer plugin, Class<T> targetClass, EventListenerFactory dispatcherFactory) {
            super(definition, plugin, targetClass, dispatcherFactory);
            this.compoundList = CollectionUtil.compoundList(parent.getRegisteredTypes(), components);
            this.root = parent.getRoot();
            this.parent = parent;
        }

        @Override
        void init(Map<Class<?>, List<EventListener<?, T>>> events) {
            if (parent.primer != null) throw new IllegalStateException();

            final var bIdx = parent.getComponentCount();
            for (var i = 0; i < components.size(); i++) components.get(i).init(bIdx + i);

            final var ext = new EventListener[parent.eventListeners.length][];
            final var own = new ArrayList<Map.Entry<Class<?>, List<EventListener<?, T>>>>();

            for (var entry : events.entrySet()) {
                final var fromParent = parent.eventTypes.get(entry.getKey());
                if (fromParent == null) {
                    own.add(entry);
                } else {
                    final var ownList = entry.getValue();
                    final var parentArr = parent.eventListeners[fromParent.internalId];
                    final var ownArr = new EventListener[ownList.size() + 1];
                    for (int i = 0; i < ownList.size(); i++) ownArr[i] = ownList.get(i);
                    ownArr[ownArr.length - 1] = new EventListener.Compound(parentArr, parent);
                    ext[fromParent.internalId] = ownArr;
                }
            }

            this.eventListeners = new EventListener[ext.length + own.size()][];
            for (var i = 0; i < ext.length; i++) {
                this.eventListeners[i] = ext[i] == null ? parent.eventListeners[i] : ext[i];
            }

            this.eventTypes = new HashMap<>(own.size());
            for (var i = 0; i < own.size(); i++) {
                final var entry = own.get(i);
                final var idx = ext.length + i;
                this.eventListeners[idx] = entry.getValue().toArray(EventListener[]::new);
                this.eventTypes.put(entry.getKey(), new EventType<>(entry.getKey(), idx));
            }
        }

        @Override
        void rebuildEventListeners(EventListenerFactory listenerFactory) {
            super.rebuildEventListeners(listenerFactory);
            final var fromParents = new ArrayList<EventType<?, ?>>();
            parent.collectEventTypes(fromParents);
            for (var evt : fromParents) {
                final var listeners = eventListeners[evt.internalId];
                if (parent.eventListeners[evt.internalId] != listeners) {
                    for (int i = 0; i < listeners.length; i++) {
                        if (listeners[i] instanceof EventListener.Base) {
                            final var base = (EventListener.Base) listeners[i];
                            listeners[i] = rebuildListener(base, evt.eventClass, listenerFactory);
                        }
                    }
                }
            }
        }

        @Override
        void collectEventTypes(Collection<EventType<?, ?>> collection) {
            super.collectEventTypes(collection);
            parent.collectEventTypes(collection);
        }

        @Override
        public <E> EventType<E, T> getEventTypeInternal(Class<E> eventClass) {
            final var own = eventTypes.get(eventClass);
            return (EventType<E, T>) (own == null ? parent.getEventTypeInternal(eventClass) : own);
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

        @Override
        public boolean canAcceptFrom(TargetType<?> other) {
            return other == this || parent.canAcceptFrom(other);
        }

    }

}
