package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentTargetType;

public interface EventTypeFactory<U> {

    <T, E extends U> EventType<E, T> build(ComponentTargetType<T> targetType, Class<E> eventClass);

    Class<U> getTarget();

}
