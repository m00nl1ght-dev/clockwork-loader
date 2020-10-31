package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.debug.profiler.EventBusProfilerGroup;
import dev.m00nl1ght.clockwork.debug.profiler.Profilable;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.EventListenerPriority;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Set;
import java.util.function.BiConsumer;

public interface EventBus<B extends Event> extends Profilable<EventBusProfilerGroup> {

    Set<EventDispatcher<? extends B, ?>> getEventDispatchers();

    <E extends B, T extends ComponentTarget> EventDispatcher<E, T>
    getEventDispatcher(TypeRef<E> eventType, Class<T> targetClass);

    default <E extends B, T extends ComponentTarget> EventDispatcher<E, T>
    getEventDispatcher(Class<E> eventClass, Class<T> targetClass) {
        return getEventDispatcher(TypeRef.of(eventClass), targetClass);
    }

    <E extends B, O extends ComponentTarget, T extends ComponentTarget>
    NestedEventDispatcher<E, T, O> getNestedEventDispatcher(TypeRef<E> eventType, Class<T> targetClass, Class<O> originClass);

    default <E extends B, O extends ComponentTarget, T extends ComponentTarget>
    NestedEventDispatcher<E, T, O> getNestedEventDispatcher(Class<E> eventClass, Class<T> targetClass, Class<O> originClass) {
        return getNestedEventDispatcher(TypeRef.of(eventClass), targetClass, originClass);
    }

    <E extends B, O extends ComponentTarget, T extends ComponentTarget>
    StaticEventDispatcher<E, T, O> getStaticEventDispatcher(TypeRef<E> eventType, Class<T> targetClass, Class<O> originClass, T target);

    default <E extends B, O extends ComponentTarget, T extends ComponentTarget>
    StaticEventDispatcher<E, T, O> getStaticEventDispatcher(Class<E> eventClass, Class<T> targetClass, Class<O> originClass, T target) {
        return getStaticEventDispatcher(TypeRef.of(eventClass), targetClass, originClass, target);
    }

    <E extends B, T extends ComponentTarget, C>
    EventListener<E, ? extends T, C> addListener(TypeRef<E> eventType, Class<T> targetClass, Class<C> componentClass, BiConsumer<C, E> consumer, EventListenerPriority priority);

    default <E extends B, T extends ComponentTarget, C>
    EventListener<E, ? extends T, C> addListener(TypeRef<E> eventType, Class<T> targetClass, Class<C> componentClass, BiConsumer<C, E> consumer) {
        return addListener(eventType, targetClass, componentClass, consumer, EventListenerPriority.NORMAL);
    }

    default <E extends B, T extends ComponentTarget, C>
    EventListener<E, ? extends T, C> addListener(Class<E> eventClass, Class<T> targetClass, Class<C> componentClass, BiConsumer<C, E> consumer, EventListenerPriority priority) {
        return addListener(TypeRef.of(eventClass), targetClass, componentClass, consumer, priority);
    }

    default <E extends B, T extends ComponentTarget, C>
    EventListener<E, ? extends T, C> addListener(Class<E> eventClass, Class<T> targetClass, Class<C> componentClass, BiConsumer<C, E> consumer) {
        return addListener(TypeRef.of(eventClass), targetClass, componentClass, consumer, EventListenerPriority.NORMAL);
    }

    <E extends B, O extends ComponentTarget, T extends ComponentTarget, C>
    EventListener<E, ? extends T, C> addNestedListener(TypeRef<E> eventType, Class<T> targetClass, Class<O> originClass, Class<C> componentClass, BiConsumer<C, E> consumer, EventListenerPriority priority);

    default <E extends B, O extends ComponentTarget, T extends ComponentTarget, C>
    EventListener<E, ? extends T, C> addNestedListener(TypeRef<E> eventType, Class<T> targetClass, Class<O> originClass, Class<C> componentClass, BiConsumer<C, E> consumer) {
        return addNestedListener(eventType, targetClass, originClass, componentClass, consumer, EventListenerPriority.NORMAL);
    }

    default <E extends B, O extends ComponentTarget, T extends ComponentTarget, C>
    EventListener<E, ? extends T, C> addNestedListener(Class<E> eventClass, Class<T> targetClass, Class<O> originClass, Class<C> componentClass, BiConsumer<C, E> consumer, EventListenerPriority priority) {
        return addNestedListener(TypeRef.of(eventClass), targetClass, originClass, componentClass, consumer, priority);
    }

    default <E extends B, O extends ComponentTarget, T extends ComponentTarget, C>
    EventListener<E, ? extends T, C> addNestedListener(Class<E> eventClass, Class<T> targetClass, Class<O> originClass, Class<C> componentClass, BiConsumer<C, E> consumer) {
        return addNestedListener(TypeRef.of(eventClass), targetClass, originClass, componentClass, consumer, EventListenerPriority.NORMAL);
    }

    <E extends B, O extends ComponentTarget, T extends ComponentTarget, C>
    EventListener<E, ? extends T, C> addStaticListener(TypeRef<E> eventType, Class<T> targetClass, Class<O> originClass, T target, Class<C> componentClass, BiConsumer<C, E> consumer, EventListenerPriority priority);

    default <E extends B, O extends ComponentTarget, T extends ComponentTarget, C>
    EventListener<E, ? extends T, C> addStaticListener(TypeRef<E> eventType, Class<T> targetClass, Class<O> originClass, T target, Class<C> componentClass, BiConsumer<C, E> consumer) {
        return addStaticListener(eventType, targetClass, originClass, target, componentClass, consumer, EventListenerPriority.NORMAL);
    }

    default <E extends B, O extends ComponentTarget, T extends ComponentTarget, C>
    EventListener<E, ? extends T, C> addStaticListener(Class<E> eventClass, Class<T> targetClass, Class<O> originClass, T target, Class<C> componentClass, BiConsumer<C, E> consumer, EventListenerPriority priority) {
        return addStaticListener(TypeRef.of(eventClass), targetClass, originClass, target, componentClass, consumer, priority);
    }

    default <E extends B, O extends ComponentTarget, T extends ComponentTarget, C>
    EventListener<E, ? extends T, C> addStaticListener(Class<E> eventClass, Class<T> targetClass, Class<O> originClass, T target, Class<C> componentClass, BiConsumer<C, E> consumer) {
        return addStaticListener(TypeRef.of(eventClass), targetClass, originClass, target, componentClass, consumer, EventListenerPriority.NORMAL);
    }

}
