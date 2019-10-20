package dev.m00nl1ght.clockwork.core;

import java.util.Optional;

public interface ComponentTarget<T extends ComponentTarget<T>> {

    <C> Optional<C> getComponent(ComponentType<C, T> componentType);

}
