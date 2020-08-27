package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

public class ComponentContainer<T extends ComponentTarget> {

    protected final TargetType<T> targetType;
    protected final Object[] components;
    protected final T object;

    public ComponentContainer(TargetType<T> targetType, T object) {
        this.targetType = Preconditions.notNull(targetType, "targetType");
        targetType.getClockworkCore().getState().requireOrAfter(ClockworkCore.State.POPULATED);
        Preconditions.verifyType(Preconditions.notNull(object, "object").getClass(), targetType.getTargetClass(), "object");
        this.components = new Object[targetType.getAllComponentTypes().size()];
        this.object = object;
    }

    public void initComponents() { // TODO restrict access (controller?)
        for (var comp : targetType.getAllComponentTypes()) {
            try {
                final var factory = comp.getFactory();
                components[comp.getInternalID()] = factory == null ? null : factory.create(object);
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
