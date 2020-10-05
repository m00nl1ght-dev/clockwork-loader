package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Arguments;

public abstract class ComponentContainer<T extends ComponentTarget> {

    protected final TargetType<T> targetType;

    protected ComponentContainer(TargetType<T> targetType) {
        this.targetType = Arguments.notNull(targetType, "targetType");
        targetType.requireInitialised();
    }

    public abstract Object getComponent(int internalID);

    @SuppressWarnings("unchecked")
    public T getTarget() {
        return (T) getComponent(0);
    }

    public final TargetType<T> getTargetType() {
        return targetType;
    }

    protected <C> C buildComponent(ComponentType<C, ? super T> componentType, T object) throws Throwable {
        return componentType.factory.create(object);
    }

}
