package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.debug.profiler.EventBusProfilerGroup;
import dev.m00nl1ght.clockwork.debug.profiler.Profilable;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Set;

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

}
