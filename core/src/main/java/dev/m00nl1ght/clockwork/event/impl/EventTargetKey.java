package dev.m00nl1ght.clockwork.event.impl;

import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.TargetType;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.utils.reflect.TypeRef;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class EventTargetKey<E extends Event, T extends ComponentTarget> {

    private final TypeRef<E> eventType;
    private final TargetType<T> targetType;

    public static <E extends Event, T extends ComponentTarget>
    @NotNull EventTargetKey<E, T> of(@NotNull Class<E> eventClass, @NotNull TargetType<T> targetType) {
        return new EventTargetKey<>(TypeRef.of(eventClass), targetType);
    }

    public static <E extends Event, T extends ComponentTarget>
    @NotNull EventTargetKey<E, T> of(@NotNull TypeRef<E> eventType, @NotNull TargetType<T> targetType) {
        return new EventTargetKey<>(eventType, targetType);
    }

    private EventTargetKey(@NotNull TypeRef<E> eventType, @NotNull TargetType<T> targetType) {
        this.eventType = Objects.requireNonNull(eventType);
        this.targetType = Objects.requireNonNull(targetType);
    }

    public @NotNull TargetType<T> getTargetType() {
        return targetType;
    }

    public @NotNull TypeRef<E> getEventType() {
        return eventType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventTargetKey)) return false;
        final var key = (EventTargetKey) o;
        return eventType.equals(key.eventType) &&
                targetType == key.targetType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, targetType);
    }

}
