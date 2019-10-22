package dev.m00nl1ght.clockwork.core;

import java.util.Optional;

public interface ComponentTarget<T extends ComponentTarget<T>> {

    <C> C getComponent(ComponentType<C, T> componentType);

    default <C> Optional<C> getComponentOptional(ComponentType<C, T> componentType) {
        return Optional.ofNullable(getComponent(componentType));
    }

}
