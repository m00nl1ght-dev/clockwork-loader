package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.EventBusProfilerGroup;
import dev.m00nl1ght.clockwork.debug.profiler.Profilable;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.EventListenerPriority;
import dev.m00nl1ght.clockwork.events.listener.SimpleEventListener;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.BiConsumer;

public interface EventBus<B extends Event> extends Profilable<EventBusProfilerGroup> {

    @NotNull Set<@NotNull EventDispatcher<? extends B, ?>> getEventDispatchers();

    <E extends B, T extends ComponentTarget>
    @NotNull EventDispatcher<E, T> getEventDispatcher(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType);

    default <E extends B, T extends ComponentTarget>
    @NotNull EventDispatcher<E, T> getEventDispatcher(
            @NotNull Class<E> eventClass,
            @NotNull TargetType<T> targetType) {

        return getEventDispatcher(TypeRef.of(eventClass), targetType);
    }

    <E extends B, T extends ComponentTarget>
    @NotNull EventListenerCollection<E, T> getListenerCollection(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType);

    default <E extends B, T extends ComponentTarget>
    @NotNull EventListenerCollection<E, T> getListenerCollection(
            @NotNull Class<E> eventClass,
            @NotNull TargetType<T> targetType) {

        return getListenerCollection(TypeRef.of(eventClass), targetType);
    }

    default <E extends B, T extends ComponentTarget, C>
    boolean addListener(@NotNull EventListener<E, T, C> listener) {
        final var targetType = listener.getComponentType().getTargetType();
        return getListenerCollection(listener.getEventType(), targetType).add(listener);
    }

    default <E extends B, T extends ComponentTarget, C>
    @NotNull EventListener<E, T, C> addListener(
            @NotNull TypeRef<E> eventType,
            @NotNull ComponentType<C, T> componentType,
            @NotNull BiConsumer<C, E> consumer,
            @NotNull EventListenerPriority priority) {

        final var listener = new SimpleEventListener<>(eventType, componentType, priority, consumer);
        addListener(listener);
        return listener;
    }

    default <E extends B, T extends ComponentTarget, C>
    @NotNull EventListener<E, T, C> addListener(
            @NotNull TypeRef<E> eventType,
            @NotNull ComponentType<C, T> componentType,
            @NotNull BiConsumer<C, E> consumer) {

        return addListener(eventType, componentType, consumer, EventListenerPriority.NORMAL);
    }

    default <E extends B, T extends ComponentTarget, C>
    @NotNull EventListener<E, T, C> addListener(
            @NotNull Class<E> eventClass,
            @NotNull ComponentType<C, T> componentType,
            @NotNull BiConsumer<C, E> consumer,
            @NotNull EventListenerPriority priority) {

        return addListener(TypeRef.of(eventClass), componentType, consumer, priority);
    }

    default <E extends B, T extends ComponentTarget, C>
    @NotNull EventListener<E, T, C> addListener(
            @NotNull Class<E> eventClass,
            @NotNull ComponentType<C, T> componentType,
            @NotNull BiConsumer<C, E> consumer) {

        return addListener(TypeRef.of(eventClass), componentType, consumer, EventListenerPriority.NORMAL);
    }

    default <E extends B, T extends ComponentTarget, C>
    @NotNull EventListener<E, T, C> addListener(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer,
            @NotNull EventListenerPriority priority) {

        final var component = targetType.getOwnComponentTypeOrThrow(componentClass);
        return addListener(eventType, component, consumer, priority);
    }

    default <E extends B, T extends ComponentTarget, C>
    @NotNull EventListener<E, T, C> addListener(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer) {

        return addListener(eventType, targetType, componentClass, consumer, EventListenerPriority.NORMAL);
    }

    default <E extends B, T extends ComponentTarget, C>
    @NotNull EventListener<E, T, C> addListener(
            @NotNull Class<E> eventClass,
            @NotNull TargetType<T> targetType,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer,
            @NotNull EventListenerPriority priority) {

        return addListener(TypeRef.of(eventClass), targetType, componentClass, consumer, priority);
    }

    default <E extends B, T extends ComponentTarget, C>
    @NotNull EventListener<E, T, C> addListener(
            @NotNull Class<E> eventClass,
            @NotNull TargetType<T> targetType,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer) {

        return addListener(TypeRef.of(eventClass), targetType, componentClass, consumer, EventListenerPriority.NORMAL);
    }

}
