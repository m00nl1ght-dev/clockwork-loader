package dev.m00nl1ght.clockwork.core;

import java.util.Optional;

public interface ComponentTarget {

    ComponentTargetType<?> getTargetType();

    <C> C getComponent(ComponentType<C, ?> componentType);

    default <C> Optional<C> getComponentOptional(ComponentType<C, ?> componentType) {
        return Optional.ofNullable(getComponent(componentType));
    }

}
