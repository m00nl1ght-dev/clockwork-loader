package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.CollectionUtil;
import dev.m00nl1ght.clockwork.util.LogUtil;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class TargetType<T extends ComponentTarget> {

    protected final String id;
    protected final Class<T> targetClass;
    protected final PluginContainer plugin;
    protected final ArrayList<ComponentType<?, T>> components = new ArrayList<>();
    protected Map<Class, EventType> eventTypes;
    protected Map<Class, FunctionalSubtarget> subtargetTypes;
    protected EventListener[][] eventListeners;
    protected int[][] subtargetData;
    private Primer<T> primer;

    private TargetType(TargetDefinition definition, PluginContainer plugin, Class<T> targetClass, EventListenerFactory dispatcherFactory) {
        this.plugin = plugin;
        this.targetClass = targetClass;
        this.id = definition.getId();
        this.primer = new Primer<>(this, dispatcherFactory);
    }

    public abstract List<ComponentType<?, ? super T>> getRegisteredTypes();

    public abstract int getComponentCount();

    public abstract TargetType<? super T> getParent();

    public abstract TargetType<? super T> getRoot();

    public abstract boolean canAcceptFrom(TargetType<?> other);

    @SuppressWarnings("unchecked")
    public <E> EventType<E, T> getEventType(Class<E> eventClass) { // TODO return empty/NOOP event type instead of null
        Preconditions.notNull(eventClass, "eventClass");
        if (eventTypes == null) throw new IllegalStateException();
        return (EventType<E, T>) eventTypes.get(eventClass);
    }

    @SuppressWarnings("unchecked")
    public <F> FunctionalSubtarget<T, F> getSubtarget(Class<F> type) { // TODO return empty/NOOP subtarget instead of null
        Preconditions.notNull(type, "type");
        if (subtargetTypes == null) throw new IllegalStateException();
        return (FunctionalSubtarget<T, F>) subtargetTypes.get(type);
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
                this.checkCompatibility(object.getTargetType());
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
        final var compIds = subtargetData[internalId];
        for (var compId : compIds) {
            try {
                final var comp = object.getComponent(compId);
                if (comp != null) consumer.accept((F) comp);
            } catch (ExceptionInPlugin e) {
                throw e;
            } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
                this.checkCompatibility(object.getTargetType());
                throw e;
            } catch (Throwable t) {
                throw ExceptionInPlugin.inFunctionalSubtarget(components.get(compId), type, t);
            }
        }
    }

    void checkCompatibility(TargetType<?> other) {
        if (!this.canAcceptFrom(other)) {
            final var msg = "Component target [] cannot post event to component in different target []";
            throw new IllegalArgumentException(LogUtil.format(msg, id, other));
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

    abstract void init();

    @SuppressWarnings("unchecked")
    void rebuildEventListeners(EventListenerFactory listenerFactory) {
        if (eventTypes != null)
        for (var evt : eventTypes.entrySet()) {
            final var listeners = eventListeners[evt.getValue().getInternalId()];
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
        private final Map<Class<?>, List<ComponentType<?, T>>> subtargets = new HashMap<>();

        private Primer(TargetType<T> pre, EventListenerFactory listenerFactory) {
            this.listenerFactory = listenerFactory;
            this.pre = pre;
        }

        public synchronized <C, E> void registerListener(ComponentType<C, T> componentType, Class<E> eventClass, BiConsumer<C, E> consumer, EventFilter<E, T> filter) {
            if (pre.isInitialised()) throw new IllegalStateException();
            if (componentType.getTargetType() != pre) throw new IllegalArgumentException();
            final var list = events.computeIfAbsent(eventClass, c -> new ArrayList<>(5));
            list.add(listenerFactory.build(componentType, eventClass, consumer, filter));
        }

        public synchronized <C, F> void registerSubtarget(ComponentType<C, T> componentType, Class<F> type) {
            if (pre.isInitialised()) throw new IllegalStateException();
            if (componentType.getTargetType() != pre) throw new IllegalArgumentException();
            if (!type.isAssignableFrom(componentType.getComponentClass())) throw new IllegalArgumentException();
            final var list = subtargets.computeIfAbsent(type, c -> new ArrayList<>(5));
            list.add(componentType);
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
            pre.init();
            pre.primer = null;
        }

    }

    private static final class Root<T extends ComponentTarget> extends TargetType<T> {

        protected final List<ComponentType<?, ? super T>> publicList = Collections.unmodifiableList(components);

        private Root(TargetDefinition definition, PluginContainer plugin, Class<T> targetClass, EventListenerFactory dispatcherFactory) {
            super(definition, plugin, targetClass, dispatcherFactory);
        }

        @Override
        void init() {
            for (var i = 0; i < components.size(); i++) components.get(i).init(i);

            final var events = this.getPrimer().events;
            this.eventListeners = new EventListener[events.size()][];
            this.eventTypes = new HashMap<>(events.size());
            var i = 0; for (var entry : events.entrySet()) {
                this.eventTypes.put(entry.getKey(), new EventType<>(entry.getKey(), this, i));
                this.eventListeners[i] = entry.getValue().toArray(EventListener[]::new);
                i++;
            }

            final var subtargets = this.getPrimer().subtargets;
            this.subtargetData = new int[subtargets.size()][];
            this.subtargetTypes = new HashMap<>(subtargets.size());
            var k = 0; for (var entry : subtargets.entrySet()) {
                final var list = entry.getValue();
                this.subtargetTypes.put(entry.getKey(), new FunctionalSubtarget<>(entry.getKey(), this, k));
                this.subtargetData[k] = list.stream().mapToInt(ComponentType::getInternalID).toArray();
                k++;
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
        void init() {
            if (parent.primer != null) throw new IllegalStateException();

            final var bIdx = parent.getComponentCount();
            for (var i = 0; i < components.size(); i++) components.get(i).init(bIdx + i);

            final var events = this.getPrimer().events;
            final var extE = new EventListener[parent.eventListeners.length][];
            final var ownE = new ArrayList<Map.Entry<Class<?>, List<EventListener<?, T>>>>();

            for (var entry : events.entrySet()) {
                final var fromParent = parent.eventTypes.get(entry.getKey());
                if (fromParent == null) {
                    ownE.add(entry);
                } else {
                    final var ownList = entry.getValue();
                    final var parentArr = parent.eventListeners[fromParent.getInternalId()];
                    final var ownArr = new EventListener[ownList.size() + 1];
                    for (int i = 0; i < ownList.size(); i++) ownArr[i] = ownList.get(i);
                    ownArr[ownArr.length - 1] = new EventListener.Compound<>(parentArr, parent);
                    extE[fromParent.getInternalId()] = ownArr;
                }
            }

            this.eventListeners = new EventListener[extE.length + ownE.size()][];
            for (var i = 0; i < extE.length; i++) {
                this.eventListeners[i] = extE[i] == null ? parent.eventListeners[i] : extE[i];
            }

            this.eventTypes = new HashMap<>(ownE.size());
            for (var i = 0; i < ownE.size(); i++) {
                final var entry = ownE.get(i);
                final var idx = extE.length + i;
                this.eventListeners[idx] = entry.getValue().toArray(EventListener[]::new);
                this.eventTypes.put(entry.getKey(), new EventType<>(entry.getKey(), this, idx));
            }

            final var subtargets = this.getPrimer().subtargets;
            final var extS = new int[parent.subtargetData.length][];
            final var ownS = new ArrayList<Map.Entry<Class<?>, List<ComponentType<?, T>>>>();

            for (var entry : subtargets.entrySet()) {
                final var fromParent = parent.subtargetTypes.get(entry.getKey());
                if (fromParent == null) {
                    ownS.add(entry);
                } else {
                    final var ownList = entry.getValue();
                    final var parentArr = parent.subtargetData[fromParent.getInternalId()];
                    final var arr = new int[parentArr.length + ownList.size()];
                    System.arraycopy(parentArr, 0, arr, 0, parentArr.length);
                    for (int i = 0; i < ownList.size(); i++) arr[parentArr.length + i] = ownList.get(i).getInternalID();
                    extS[fromParent.getInternalId()] = arr;
                }
            }

            this.subtargetData = new int[extS.length + ownS.size()][];
            for (var i = 0; i < extS.length; i++) {
                this.subtargetData[i] = extS[i] == null ? parent.subtargetData[i] : extS[i];
            }

            this.subtargetTypes = new HashMap<>(ownS.size());
            for (var i = 0; i < ownS.size(); i++) {
                final var entry = ownS.get(i);
                final var idx = extS.length + i;
                this.subtargetData[idx] = entry.getValue().stream().mapToInt(ComponentType::getInternalID).toArray();
                this.subtargetTypes.put(entry.getKey(), new FunctionalSubtarget<>(entry.getKey(), this, idx));
            }
        }

        @Override
        void rebuildEventListeners(EventListenerFactory listenerFactory) {
            super.rebuildEventListeners(listenerFactory);
            for (var pt = this.parent; pt != null; pt = pt.getParent()) {
                for (var evt : pt.eventTypes.entrySet()) {
                    final int internalId = evt.getValue().getInternalId();
                    final var listeners = eventListeners[internalId];
                    if (pt.eventListeners[internalId] != listeners) {
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

        public <E> EventType<E, T> getEventType(Class<E> eventClass) {
            final var own = super.getEventType(eventClass);
            return own == null ? (EventType<E, T>) parent.eventTypes.get(eventClass) : own;
        }

        public <F> FunctionalSubtarget<T, F> getSubtarget(Class<F> type) {
            final var own = super.getSubtarget(type);
            return own == null ? (FunctionalSubtarget<T, F>) parent.subtargetTypes.get(type) : own;
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
