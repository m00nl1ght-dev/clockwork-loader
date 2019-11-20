package dev.m00nl1ght.clockwork.core;

import java.util.Optional;

public interface ComponentTarget<T extends ComponentTarget<? super T>> {

    TargetType<? extends T> getTargetType();

    <C> C getComponent(ComponentType<C, ? extends T> componentType);

    default <C> Optional<C> getComponentOptional(ComponentType<C, ? extends T> componentType) {
        return Optional.ofNullable(getComponent(componentType));
    }

}
