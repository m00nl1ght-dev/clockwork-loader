package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

public class ComponentContainer<T extends ComponentTarget> {

    protected final TargetType<T> targetType;
    protected final Object[] components;
    protected final T object;

    public ComponentContainer(TargetType<T> targetType, T object) {
        this.targetType = Preconditions.notNull(targetType, "targetType");
        targetType.getPlugin().getClockworkCore().getState().requireOrAfter(ClockworkCore.State.POPULATED);
        Preconditions.verifyType(Preconditions.notNull(object, "object").getClass(), targetType.getTargetClass(), "object");
        this.components = new Object[targetType.getComponentTypes().size()];
        this.object = object;
        this.initComponents();
    }

    protected void initComponents() {
        for (var comp : targetType.getComponentTypes()) {
            try {
                components[comp.getInternalID()] = comp.buildComponentFor(object);
            } catch (Throwable t) {
                throw ExceptionInPlugin.inComponentInit(comp, t);
            }
        }
    }

    public Object getComponent(int internalID) {
        return components[internalID];
    }

    public TargetType<T> getTargetType() {
        return targetType;
    }

}
