package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.event.listener.EventListener;
import dev.m00nl1ght.clockwork.util.CollectionUtil;
import dev.m00nl1ght.clockwork.util.LogUtil;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.*;

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

    private TargetType(TargetDefinition definition, PluginContainer plugin, Class<T> targetClass) {
        this.plugin = plugin;
        this.targetClass = targetClass;
        this.id = definition.getId();
        this.primer = new Primer<>(this);
    }

    public abstract List<ComponentType<?, ? super T>> getRegisteredTypes();

    public abstract int getComponentCount();

    public abstract TargetType<? super T> getParent();

    public abstract TargetType<? super T> getRoot();

    public abstract boolean canAcceptFrom(TargetType<?> other);

    @SuppressWarnings("unchecked")
    <E> EventType<E, T> getEventTypeNullable(Class<E> eventClass) {
        if (eventTypes == null) throw new IllegalStateException();
        return (EventType<E, T>) eventTypes.get(eventClass);
    }

    public final <E> EventType<E, T> getEventType(Class<E> eventClass) {
        Preconditions.notNull(eventClass, "eventClass");
        final var ret = getEventTypeNullable(eventClass);
        return ret == null ? new EventType.Empty<>(eventClass, this.getRoot()) : ret;
    }

    @SuppressWarnings("unchecked")
    <F> FunctionalSubtarget<T, F> getSubtargetNullable(Class<F> type) {
        if (subtargetTypes == null) throw new IllegalStateException();
        return (FunctionalSubtarget<T, F>) subtargetTypes.get(type);
    }

    public final <F> FunctionalSubtarget<T, F> getSubtarget(Class<F> type) {
        Preconditions.notNull(type, "type");
        final var ret = getSubtargetNullable(type);
        return ret == null ? new FunctionalSubtarget.Empty<>(type, this.getRoot()) : ret;
    }

    void checkCompatibilityForEvent(TargetType<?> other) {
        if (!this.canAcceptFrom(other)) {
            final var msg = "Component target [] cannot post event to component in different target []";
            throw new IllegalArgumentException(LogUtil.format(msg, id, other));
        }
    }

    void checkCompatibilityForSubtarget(TargetType<?> other) {
        if (!this.canAcceptFrom(other)) {
            final var msg = "Component target [] cannot apply subtarget to component in different target []";
            throw new IllegalArgumentException(LogUtil.format(msg, id, other));
        }
    }

    void checkCompatibilityForComponent(TargetType<?> other) {
        if (!this.canAcceptFrom(other)) {
            final var msg = "Component target [] cannot get component in different target []";
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
    static <T extends ComponentTarget> TargetType<T> create(TargetDefinition def, PluginContainer plugin, Class<T> targetClass) {
        if (def.getParent() == null) {
            return new Root<>(def, plugin, targetClass);
        } else {
            final var found = plugin.getClockworkCore().getTargetType(def.getParent()).orElseThrow();
            if (found.targetClass.isAssignableFrom(targetClass)) {
                return new ForSubclass<>(def, (TargetType<? super T>) found, plugin, targetClass);
            } else {
                throw PluginLoadingException.invalidParentForTarget(def, found);
            }
        }
    }

    public static class Primer<T extends ComponentTarget> {

        private final TargetType<T> targetType;
        private final Map<Class<?>, List<EventListener<?, ?, T>>> events = new HashMap<>();
        private final Map<Class<?>, List<ComponentType<?, T>>> subtargets = new HashMap<>();

        private Primer(TargetType<T> targetType) {
            this.targetType = targetType;
        }

        public synchronized <C, E> void registerListener(Class<E> eventClass, EventListener<E, C, T> listener) {
            if (targetType.isInitialised()) throw new IllegalStateException();
            if (listener.getComponentType().getTargetType() != targetType) throw new IllegalArgumentException();
            final var list = events.computeIfAbsent(eventClass, c -> new ArrayList<>(5));
            list.add(listener);
        }

        public synchronized <C, F> void registerSubtarget(ComponentType<C, T> componentType, Class<F> type) {
            if (targetType.isInitialised()) throw new IllegalStateException();
            if (componentType.getTargetType() != targetType) throw new IllegalArgumentException();
            if (!type.isAssignableFrom(componentType.getComponentClass())) throw new IllegalArgumentException();
            final var list = subtargets.computeIfAbsent(type, c -> new ArrayList<>(5));
            list.add(componentType);
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

        protected final List<ComponentType<?, ? super T>> publicList = Collections.unmodifiableList(components);

        private Root(TargetDefinition definition, PluginContainer plugin, Class<T> targetClass) {
            super(definition, plugin, targetClass);
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

        private ForSubclass(TargetDefinition definition, TargetType<? super T> parent, PluginContainer plugin, Class<T> targetClass) {
            super(definition, plugin, targetClass);
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
            final var ownE = new ArrayList<Map.Entry<Class<?>, List<EventListener<?, ?, T>>>>();

            for (var entry : events.entrySet()) {
                final var fromParent = parent.eventTypes.get(entry.getKey());
                if (fromParent == null) {
                    ownE.add(entry);
                } else {
                    final var ownList = entry.getValue();
                    final var parentArr = parent.eventListeners[fromParent.getInternalId()];
                    final var arr = new EventListener[parentArr.length + ownList.size()];
                    System.arraycopy(parentArr, 0, arr, 0, parentArr.length);
                    for (int i = 0; i < ownList.size(); i++) arr[parentArr.length + i] = ownList.get(i);
                    extE[fromParent.getInternalId()] = arr;
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
        <E> EventType<E, T> getEventTypeNullable(Class<E> eventClass) {
            final var own = super.getEventTypeNullable(eventClass);
            return own == null ? (EventType<E, T>) parent.getEventTypeNullable(eventClass) : own;
        }

        @Override
        <F> FunctionalSubtarget<T, F> getSubtargetNullable(Class<F> type) {
            final var own = super.getSubtargetNullable(type);
            return own == null ? (FunctionalSubtarget<T, F>) parent.getSubtargetNullable(type) : own;
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
