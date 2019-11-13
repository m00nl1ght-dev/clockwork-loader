package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

public class ComponentContainer<T> {

    protected final ComponentTargetType<T> targetType;
    protected final Object[] components;
    protected final T object;

    public <U extends T> ComponentContainer(ComponentTargetType<T> targetType, U object) {
        this.object = Preconditions.notNull(object, "object");
        this.targetType = Preconditions.notNullAnd(targetType, ComponentTargetType::isRegistryLocked, "targetType");
        this.components = new Object[targetType.getComponentCount()];
    }

    public void initComponents() {
        for (var r : targetType.getRegisteredTypes()) {
            components[r.getInternalID()] = r.buildComponentFor(object);
        }
    }

    @SuppressWarnings("unchecked")
    public <C> C getComponent(ComponentType<C, ?> componentType) {
        if (componentType.getTargetType() != this.targetType) return null;
        return (C) components[componentType.getInternalID()];
    }

    protected <C> void setComponent(ComponentType<C, ?> componentType, C value) {
        if (componentType.getTargetType() != this.targetType) throw new IllegalArgumentException();
        components[componentType.getInternalID()] = value;
    }

}
