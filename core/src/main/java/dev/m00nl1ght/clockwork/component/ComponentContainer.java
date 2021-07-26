package dev.m00nl1ght.clockwork.component;

import java.util.Objects;

public abstract class ComponentContainer {

    protected final TargetType<?> targetType;

    protected ComponentContainer(TargetType<?> targetType) {
        this.targetType = Objects.requireNonNull(targetType);
        targetType.requireLocked();
    }

    public abstract Object getComponent(int internalID);

    public final TargetType<?> getTargetType() {
        return targetType;
    }

}
