package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.event.EventType;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.*;
import java.util.function.BiConsumer;

public final class ComponentTargetType<T> {

    private final String id;
    private final Class<T> targetClass;
    private final PluginContainer parent;
    private boolean lockRegistry = false;
    private final Map<String, ComponentType<?, T>> components = new HashMap<>();
    private final List<ComponentType<?, T>> compList = new ArrayList<>();
    private final List<ComponentType<?, T>> compListReadOnly = Collections.unmodifiableList(compList);
    private final Map<Class<?>, EventType<?, T>> events = new HashMap<>();

    public ComponentTargetType(ComponentTargetDefinition definition, PluginContainer parent, Class<T> targetClass) {
        this.parent = Preconditions.notNull(parent, "parent");
        Preconditions.notNull(definition, "definition");
        Preconditions.verifyType(targetClass, ComponentTarget.class, "targetClass");
        this.targetClass = Preconditions.notNullAnd(targetClass, o -> definition.getTargetClass().equals(o.getCanonicalName()), "targetClass");
        this.id = definition.getId();
    }

    protected <C> ComponentType<C, T> register(ComponentDefinition def, PluginContainer plugin, Class<C> compClass) {
        if (lockRegistry) throw new IllegalStateException("target type registry already locked");
        final var componentType = new ComponentType<>(def, plugin, compClass, this, compList.size());
        components.put(componentType.getId(), componentType);
        compList.add(componentType);
        return componentType;
    }

    public <E> EventType<E, T> registerEvent(Class<E> eventClass) {
        final var evt = new EventType<>(this, eventClass);
        if (events.putIfAbsent(eventClass, evt) != null) throw PluginLoadingException.generic("Event class [" + eventClass.getSimpleName() + "] already registered");
        return evt;
    }

    public <E, C> void registerEventListener(Class<E> eventClass, ComponentType<C, T> component, BiConsumer<C, E> listener) {
        final var type = getEventType(eventClass);
        type.registerListener(component, listener);
    }

    @SuppressWarnings("unchecked")
    public <E> EventType<E, T> getEventType(Class<E> eventClass) {
        final var type = events.get(eventClass);
        if (type == null) throw new IllegalArgumentException("Event type for class [" + eventClass.getSimpleName() + "] not found");
        return (EventType<E, T>) type;
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

    protected void lockRegistry() {
        lockRegistry = true;
    }

    public boolean isRegistryLocked() {
        return lockRegistry;
    }

}
