package dev.m00nl1ght.clockwork.core;

import java.util.Objects;

public abstract class ComponentContainer {

    protected final TargetType<?> targetType;

    protected ComponentContainer(TargetType<?> targetType) {
        this.targetType = Objects.requireNonNull(targetType);
        targetType.requireInitialised();
    }

    public abstract Object getComponent(int internalID);

    public final TargetType<?> getTargetType() {
        return targetType;
    }

    protected <C, T extends ComponentTarget> C
    buildComponent(ComponentType<C, T> componentType, Object target) throws Throwable {
        final var castedTarget = componentType.getTargetType().getTargetClass().cast(target);
        final var value = componentType.factory.create(castedTarget);
        componentType.checkValue(castedTarget, value);
        return value;
    }

}
