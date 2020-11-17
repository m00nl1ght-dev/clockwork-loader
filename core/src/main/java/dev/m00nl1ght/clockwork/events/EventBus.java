package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.debug.profiler.EventBusProfilerGroup;
import dev.m00nl1ght.clockwork.debug.profiler.Profilable;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.EventListenerPriority;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.BiConsumer;

public interface EventBus<B extends Event> extends Profilable<EventBusProfilerGroup> {

    @NotNull
    Set<@NotNull EventDispatcher<? extends B, ?>> getEventDispatchers();

    @NotNull
    <E extends B, T extends ComponentTarget>
    EventDispatcher<E, T> getEventDispatcher(
            @NotNull TypeRef<E> eventType,
            @NotNull Class<T> targetClass);

    @NotNull
    default <E extends B, T extends ComponentTarget>
    EventDispatcher<E, T> getEventDispatcher(
            @NotNull Class<E> eventClass,
            @NotNull Class<T> targetClass) {

        return getEventDispatcher(TypeRef.of(eventClass), targetClass);
    }

    @NotNull
    Set<@NotNull EventListenerCollection<? extends B, ?>> getListenerCollections();

    @NotNull
    <E extends B, T extends ComponentTarget>
    EventListenerCollection<E, T> getListenerCollection(
            @NotNull TypeRef<E> eventType,
            @NotNull Class<T> targetClass);

    @NotNull
    default <E extends B, T extends ComponentTarget>
    EventListenerCollection<E, T> getListenerCollection(
            @NotNull Class<E> eventClass,
            @NotNull Class<T> targetClass) {

        return getListenerCollection(TypeRef.of(eventClass), targetClass);
    }

    default <E extends B, T extends ComponentTarget, C>
    boolean addListener(@NotNull EventListener<E, T, C> listener) {
        final var targetClass = listener.getComponentType().getTargetType().getTargetClass();
        return getListenerCollection(listener.getEventType(), targetClass).add(listener);
    }

    @NotNull
    <E extends B, T extends ComponentTarget, C>
    EventListener<E, T, C> addListener(
            @NotNull TypeRef<E> eventType,
            @NotNull Class<T> targetClass,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer,
            @NotNull EventListenerPriority priority);

    @NotNull
    default <E extends B, T extends ComponentTarget, C>
    EventListener<E, T, C> addListener(
            @NotNull TypeRef<E> eventType,
            @NotNull Class<T> targetClass,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer) {

        return addListener(eventType, targetClass, componentClass, consumer, EventListenerPriority.NORMAL);
    }

    @NotNull
    default <E extends B, T extends ComponentTarget, C>
    EventListener<E, T, C> addListener(
            @NotNull Class<E> eventClass,
            @NotNull Class<T> targetClass,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer,
            @NotNull EventListenerPriority priority) {

        return addListener(TypeRef.of(eventClass), targetClass, componentClass, consumer, priority);
    }

    @NotNull
    default <E extends B, T extends ComponentTarget, C>
    EventListener<E, T, C> addListener(
            @NotNull Class<E> eventClass,
            @NotNull Class<T> targetClass,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer) {

        return addListener(TypeRef.of(eventClass), targetClass, componentClass, consumer, EventListenerPriority.NORMAL);
    }

}
