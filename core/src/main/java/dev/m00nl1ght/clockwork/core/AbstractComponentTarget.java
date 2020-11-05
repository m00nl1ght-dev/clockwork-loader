package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.container.ImmutableComponentContainer;

import java.util.Objects;

public abstract class AbstractComponentTarget<T> implements ComponentTarget {

    protected final ImmutableComponentContainer<? extends T> componentContainer;

    protected AbstractComponentTarget(TargetType<? extends T> targetType) {
        componentContainer = new ImmutableComponentContainer<>(Objects.requireNonNull(targetType), this);
    }

    @Override
    public TargetType<? extends T> getTargetType() {
        return componentContainer.getTargetType();
    }

    @Override
    public Object getComponent(int internalID) {
        return componentContainer.getComponent(internalID);
    }

}
