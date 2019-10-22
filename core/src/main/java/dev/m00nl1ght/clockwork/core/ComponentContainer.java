package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

public class ComponentContainer<T> {

    private final ComponentTargetType<T> targetType;
    private final Object[] components;
    private final T object;

    public ComponentContainer(ComponentTargetType<T> targetType, T object) {
        this.object = Preconditions.notNull(object, "object");
        this.targetType = Preconditions.notNullAnd(targetType, ComponentTargetType::isRegistryLocked, "targetType");
        this.components = new Object[targetType.getComponentCount()];
    }

    public void initComponents() {
        targetType.getRegisteredTypes().forEach(r -> components[r.getInternalID()] = r.buildComponentFor(object));
    }

    @SuppressWarnings("unchecked")
    public <C> C getComponent(ComponentType<C, T> componentType) {
        if (componentType.getTargetType() != this.targetType) return null;
        var comp = components[componentType.getInternalID()];
        if (comp == null) return null;
        return (C) comp;
    }

    protected <C> void setComponent(ComponentType<C, T> componentType, C value) {
        if (componentType.getTargetType() != this.targetType) throw new IllegalArgumentException();
        components[componentType.getInternalID()] = value;
    }

}
