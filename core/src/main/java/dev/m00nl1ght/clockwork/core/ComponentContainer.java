package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Arguments;

public abstract class ComponentContainer<T extends ComponentTarget> {

    protected final TargetType<T> targetType;

    protected ComponentContainer(TargetType<T> targetType) {
        this.targetType = Arguments.notNull(targetType, "targetType");
        targetType.getClockworkCore().getState().requireOrAfter(ClockworkCore.State.POPULATED);
    }

    public abstract Object getComponent(int internalID);

    public final TargetType<T> getTargetType() {
        return targetType;
    }

    protected <C> C buildComponent(ComponentType<C, ? super T> componentType, T object) throws Throwable {
        final var factory = componentType.getFactoryInternal();
        return factory == null ? null : factory.create(object);
    }

}
