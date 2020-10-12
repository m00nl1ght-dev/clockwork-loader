package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.debug.profiler.EventBusProfilerGroup;
import dev.m00nl1ght.clockwork.debug.profiler.Profilable;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.EventListenerPriority;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Set;
import java.util.function.BiConsumer;

public interface EventBus extends Profilable<EventBusProfilerGroup> {

    void post(Event event);

    Set<EventDispatcher<?, ?>> getEventDispatchers();

    <E extends Event, T extends ComponentTarget> EventDispatcher<E, T>
    getEventDispatcher(TypeRef<E> eventType, Class<T> targetClass);

    default <E extends Event, T extends ComponentTarget> EventDispatcher<E, T>
    getEventDispatcher(Class<E> eventClass, Class<T> targetClass) {
        return getEventDispatcher(TypeRef.of(eventClass), targetClass);
    }

    <E extends Event, T extends ComponentTarget, C> EventListener<E, T, C>
    addListener(TypeRef<E> eventType, Class<T> targetClass, Class<C> componentClass, BiConsumer<C, E> consumer, EventListenerPriority priority);

    default <E extends Event, T extends ComponentTarget, C> EventListener<E, T, C>
    addListener(TypeRef<E> eventType, Class<T> targetClass, Class<C> componentClass, BiConsumer<C, E> consumer) {
        return addListener(eventType, targetClass, componentClass, consumer, EventListenerPriority.NORMAL);
    }

    default <E extends Event, T extends ComponentTarget, C> EventListener<E, T, C>
    addListener(Class<E> eventClass, Class<T> targetClass, Class<C> componentClass, BiConsumer<C, E> consumer, EventListenerPriority priority) {
        return addListener(TypeRef.of(eventClass), targetClass, componentClass, consumer);
    }

    default <E extends Event, T extends ComponentTarget, C> EventListener<E, T, C>
    addListener(Class<E> eventClass, Class<T> targetClass, Class<C> componentClass, BiConsumer<C, E> consumer) {
        return addListener(eventClass, targetClass, componentClass, consumer, EventListenerPriority.NORMAL);
    }

}
