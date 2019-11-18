package dev.m00nl1ght.clockwork.core;

public interface EventDispatcherFactory<U> {

    <T extends ComponentTarget, E extends U> EventDispatcher<E, T> build(ComponentTargetType<T> targetType, Class<E> eventClass);

    Class<U> getTarget();

}
