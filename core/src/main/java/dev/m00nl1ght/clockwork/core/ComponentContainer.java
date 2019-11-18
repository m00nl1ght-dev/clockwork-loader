package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

public class ComponentContainer<T extends ComponentTarget> {

    protected final ComponentTargetType<T> targetType;
    protected final Object[] components;
    protected final T object;

    @SuppressWarnings("unchecked")
    public ComponentContainer(ComponentTargetType<T> targetType, Object object) {
        this.targetType = Preconditions.notNullAnd(targetType, ComponentTargetType::isPrimed, "targetType");
        Preconditions.verifyType(Preconditions.notNull(object, "object").getClass(), targetType.getTargetClass(), "object");
        this.components = new Object[targetType.getComponentCount()];
        this.object = (T) object;
    }

    public void initComponents() {
        for (var r : targetType.getRegisteredTypes()) {
            components[r.getInternalID()] = r.buildComponentFor(object);
        }
    }

    @SuppressWarnings("unchecked")
    public <C> C getComponent(ComponentType<C, ?> componentType) {
        if (componentType.getTargetType().getRoot() != this.targetType.getRoot()) return null;
        return (C) components[componentType.getInternalID()];
    }

    protected <C> void setComponent(ComponentType<C, ?> componentType, C value) {
        if (componentType.getTargetType().getRoot() != this.targetType.getRoot()) throw new IllegalArgumentException();
        components[componentType.getInternalID()] = value;
    }

    public ComponentTargetType<T> getTargetType() {
        return targetType;
    }

}
