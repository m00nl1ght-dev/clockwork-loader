package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.CollectionUtil;
import dev.m00nl1ght.clockwork.util.LogUtil;
import dev.m00nl1ght.clockwork.util.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class TargetType<T extends ComponentTarget> {

    protected final String id;
    protected final Class<T> targetClass;
    protected final PluginContainer plugin;
    protected final ArrayList<ComponentType<?, T>> components = new ArrayList<>();
    protected Object2IntMap<Class<?>> eventIds;
    protected Object2IntMap<Class<?>> subtargetIds;
    protected EventListener[][] eventListeners;
    protected int[][] subtargets;
    private Primer<T> primer;

    private TargetType(TargetDefinition definition, PluginContainer plugin, Class<T> targetClass, EventListenerFactory dispatcherFactory) {
        this.plugin = plugin;
        this.targetClass = targetClass;
        this.id = definition.getId();
        this.primer = new Primer<>(this, dispatcherFactory);
    }

    public abstract List<ComponentType<?, ? super T>> getRegisteredTypes();

    public abstract int getEventId(Class<?> eventClass);

    public abstract int getSubtargetId(Class<?> type);

    public abstract int getComponentCount();

    public abstract TargetType<? super T> getParent();

    public abstract TargetType<? super T> getRoot();

    public abstract boolean canAcceptFrom(TargetType<?> other);

    public <E> EventType<E, T> getEventType(Class<E> eventClass) {
        Preconditions.notNull(eventClass, "eventClass");
        if (eventIds == null) throw new IllegalStateException();
        return new EventType<>(eventClass, this, getEventId(eventClass));
    }

    public <F> FunctionalSubtarget<T, F> getSubtarget(Class<F> type) {
        Preconditions.notNull(type, "type");
        if (subtargetIds == null) throw new IllegalStateException();
        return new FunctionalSubtarget<>(type, this, getSubtargetId(type));
    }

    @SuppressWarnings("unchecked")
    <E, R extends ComponentTarget> void post(int internalId, R object, E event) {
        final var listeners = eventListeners[internalId];
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

    @SuppressWarnings("unchecked")
    <F, R extends ComponentTarget> void applySubtarget(int internalId, R object, Class<F> type, Consumer<F> consumer) {
        final var compIds = subtargets[internalId];
        for (var compId : compIds) {
            try {
                final var comp = object.getComponent(internalId);
                if (comp != null) consumer.accept((F) comp);
            } catch (ExceptionInPlugin e) {
                throw e;
            } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
                this.checkCompatibility(object);
                throw e;
            } catch (Throwable t) {
                throw ExceptionInPlugin.inFunctionalSubtarget(components.get(compId), type, t);
            }
        }
    }

    void checkCompatibility(ComponentTarget object) {
        if (!this.canAcceptFrom(object.getTargetType())) {
            final var msg = "Component target [] cannot post event to component in different target []";
            throw new IllegalArgumentException(LogUtil.format(msg, id, object.getTargetType()));
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

    public final boolean isInitialised() {
        return primer == null;
    }

    public final Primer<T> getPrimer() {
        return primer;
    }

    abstract void init(Map<Class<?>, List<EventListener<?, T>>> events);

    @SuppressWarnings("unchecked")
    void rebuildEventListeners(EventListenerFactory listenerFactory) {
        for (var evt : eventIds.object2IntEntrySet()) {
            final var listeners = eventListeners[evt.getIntValue()];
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] instanceof EventListener.Base) {
                    final var base = (EventListener.Base) listeners[i];
                    listeners[i] = rebuildListener(base, evt.getKey(), listenerFactory);
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
            this.eventIds = new Object2IntOpenHashMap<>(events.size());
            this.eventIds.defaultReturnValue(-1);

            var i = 0;
            for (var entry : events.entrySet()) {
                this.eventIds.put(entry.getKey(), i);
                this.eventListeners[i] = entry.getValue().toArray(EventListener[]::new);
                i++;
            }

            // TODO init subtargets

        }

        @Override
        public int getEventId(Class<?> eventClass) {
            return eventIds.getInt(eventClass);
        }

        @Override
        public int getSubtargetId(Class<?> type) {
            return subtargetIds.getInt(type);
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
                final var fromParent = parent.eventIds.getInt(entry.getKey());
                if (fromParent < 0) {
                    own.add(entry);
                } else {
                    final var ownList = entry.getValue();
                    final var parentArr = parent.eventListeners[fromParent];
                    final var ownArr = new EventListener[ownList.size() + 1];
                    for (int i = 0; i < ownList.size(); i++) ownArr[i] = ownList.get(i);
                    ownArr[ownArr.length - 1] = new EventListener.Compound(parentArr, parent);
                    ext[fromParent] = ownArr;
                }
            }

            this.eventListeners = new EventListener[ext.length + own.size()][];
            for (var i = 0; i < ext.length; i++) {
                this.eventListeners[i] = ext[i] == null ? parent.eventListeners[i] : ext[i];
            }

            this.eventIds = new Object2IntOpenHashMap<>(own.size());
            this.eventIds.defaultReturnValue(-1);
            for (var i = 0; i < own.size(); i++) {
                final var entry = own.get(i);
                final var idx = ext.length + i;
                this.eventListeners[idx] = entry.getValue().toArray(EventListener[]::new);
                this.eventIds.put(entry.getKey(), idx);
            }

            // TODO init subtargets

        }

        <F, R extends ComponentTarget> void applySubtarget(int internalId, R object, Class<F> type, Consumer<F> consumer) {
            if (internalId < parent.subtargets.length) parent.applySubtarget(internalId, object, type, consumer);
            super.applySubtarget(internalId, object, type, consumer);
        }

        @Override
        void rebuildEventListeners(EventListenerFactory listenerFactory) {
            super.rebuildEventListeners(listenerFactory);
            for (var pt = this.parent; pt != null; pt = pt.getParent()) {
                for (var evt : pt.eventIds.object2IntEntrySet()) {
                    final var listeners = eventListeners[evt.getIntValue()];
                    if (pt.eventListeners[evt.getIntValue()] != listeners) {
                        for (int i = 0; i < listeners.length; i++) {
                            if (listeners[i] instanceof EventListener.Base) {
                                final var base = (EventListener.Base) listeners[i];
                                listeners[i] = rebuildListener(base, evt.getKey(), listenerFactory);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public int getEventId(Class<?> eventClass) {
            final var own = eventIds.getInt(eventClass);
            return own < 0 ? parent.getEventId(eventClass) : own;
        }

        @Override
        public int getSubtargetId(Class<?> type) {
            final var own = subtargetIds.getInt(type);
            return own < 0 ? parent.getSubtargetId(type) : own;
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
