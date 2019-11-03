package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventType;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.*;

public final class ComponentTargetType<T> {

    private final String id;
    private final Class<T> targetClass;
    private final PluginContainer parent;
    private boolean lockRegistry = false;
    private final Map<String, ComponentType<?, T>> components = new HashMap<>();
    private final ArrayList<ComponentType<?, T>> compList = new ArrayList<>();
    private final List<ComponentType<?, T>> compListReadOnly = Collections.unmodifiableList(compList);
    private final Map<Class<?>, EventType<?, T>> eventTypes = new HashMap<>();

    public ComponentTargetType(ComponentTargetDefinition definition, PluginContainer parent, Class<T> targetClass) {
        this.parent = Preconditions.notNull(parent, "parent");
        Preconditions.notNull(definition, "definition");
        Preconditions.verifyType(targetClass, ComponentTarget.class, "targetClass");
        this.targetClass = Preconditions.notNullAnd(targetClass, o -> definition.getTargetClass().equals(o.getCanonicalName()), "targetClass");
        this.id = definition.getId();
    }

    protected synchronized <C> ComponentType<C, T> register(ComponentDefinition def, PluginContainer plugin, Class<C> compClass) {
        if (lockRegistry) throw new IllegalStateException("target type registry already locked");
        final var componentType = new ComponentType<>(def, plugin, compClass, this, compList.size());
        components.put(componentType.getId(), componentType);
        compList.add(componentType);
        return componentType;
    }

    @SuppressWarnings("unchecked")
    public <E> EventType<E, T> getEventType(Class<E> eventClass) {
        Preconditions.notNull(eventClass, "eventClass");
        if (!Event.class.isAssignableFrom(eventClass)) throw new IllegalArgumentException("eventClass must be a sublass of Event.class");
        return (EventType<E, T>) eventTypes.computeIfAbsent(eventClass, k -> new EventType(this, k));
    }

    public <F> FunctionalSubtarget<T, F> getSubtarget(Class<F> type) {
        Preconditions.notNull(type, "type");
        return new FunctionalSubtarget<>(this, type);
    }

    public PluginContainer getParent() {
        return parent;
    }

    public String getId() {
        return id;
    }

    public Class<T> getTargetClass() {
        return targetClass;
    }

    public List<ComponentType<?, T>> getRegisteredTypes() {
        return compListReadOnly;
    }

    protected int getComponentCount() {
        return compList.size();
    }

    protected ComponentType<?, T> get(int internalID) {
        return compList.get(internalID);
    }

    protected synchronized void lockRegistry() {
        lockRegistry = true;
        compList.trimToSize();
    }

    protected boolean isRegistryLocked() {
        return lockRegistry;
    }

}
