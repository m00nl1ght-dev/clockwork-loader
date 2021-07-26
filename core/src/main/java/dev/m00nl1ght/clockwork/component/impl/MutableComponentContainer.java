package dev.m00nl1ght.clockwork.component.impl;

import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.ComponentType;
import dev.m00nl1ght.clockwork.component.TargetType;

public class MutableComponentContainer extends SimpleComponentContainer {

    public MutableComponentContainer(TargetType<?> targetType, Object object) {
        super(targetType, object);
    }

    public <C, T extends ComponentTarget>
    void setComponent(ComponentType<C, T> componentType, C component) {
        final var castedTarget = componentType.getTargetType().getTargetClass().cast(this.components[0]);
        final var internalIdx = componentType.getInternalIdx(targetType);
        componentType.checkValue(castedTarget, component);
        components[internalIdx] = component;
    }

}
