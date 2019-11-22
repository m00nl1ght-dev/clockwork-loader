package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

public class ComponentContainer<T extends ComponentTarget> {

    protected final TargetType<T> targetType;
    protected final Object[] components;
    protected final T object;

    public ComponentContainer(TargetType<T> targetType, T object) {
        this.targetType = Preconditions.notNullAnd(targetType, TargetType::isInitialised, "targetType");
        Preconditions.verifyType(Preconditions.notNull(object, "object").getClass(), targetType.getTargetClass(), "object");
        this.components = new Object[targetType.getComponentCount()];
        this.object = object;
    }

    public void initComponents() {
        ComponentType<?, ?> blame = null;
        try {
            for (var comp : targetType.getRegisteredTypes()) {
                blame = comp;
                components[comp.getInternalID()] = comp.buildComponentFor(object);
            }
        } catch (Throwable t) {
            throw ExceptionInPlugin.inComponentInit(blame, t);
        }
    }

    public Object getComponent(int internalID) {
        return components[internalID];
    }

    public TargetType<T> getTargetType() {
        return targetType;
    }

}
