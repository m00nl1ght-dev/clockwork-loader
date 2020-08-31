package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Arguments;

public abstract class ComponentContainer<T extends ComponentTarget> {

    protected final TargetType<T> targetType;
    protected final T object;

    protected ComponentContainer(TargetType<T> targetType, T object) {
        this.targetType = Arguments.notNull(targetType, "targetType");
        this.object = Arguments.notNull(object, "object");
        targetType.getClockworkCore().getState().requireOrAfter(ClockworkCore.State.POPULATED);
        Arguments.verifyType(object.getClass(), targetType.getTargetClass(), "object");
    }

    public abstract void initComponents(); // TODO restrict access (controller?)

    public abstract Object getComponent(int internalID);

    public final TargetType<T> getTargetType() {
        return targetType;
    }

    protected <C> C buildComponent(ComponentType<C, ? super T> componentType) throws Throwable {
        final var factory = componentType.getFactoryInternal();
        return factory == null ? null : factory.create(object);
    }

}
