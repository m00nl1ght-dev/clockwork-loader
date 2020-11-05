package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractEventBus<B extends Event> implements EventBus<B> {

    protected final Map<Object, EventDispatcher<? extends B, ?>> dispatchers = new LinkedHashMap<>();

    @Override
    public <E extends B, T extends ComponentTarget>
    EventDispatcher<E, T> getEventDispatcher(TypeRef<E> eventType, Class<T> targetClass) {
        final var key = new EventDispatcher.Key(eventType, targetClass);
        final var existing = dispatchers.get(key);
        if (existing != null) {
            @SuppressWarnings("unchecked")
            final var casted = (EventDispatcher<E, T>) existing;
            return casted;
        } else {
            final var dispatcher = buildDispatcher(eventType, targetClass);
            dispatchers.put(key, dispatcher);
            return dispatcher;
        }
    }

    @Override
    public <E extends B, O extends ComponentTarget, T extends ComponentTarget>
    NestedEventDispatcher<E, T, O> getNestedEventDispatcher(TypeRef<E> eventType, Class<T> targetClass, Class<O> originClass) {
        final var key = new NestedEventDispatcher.Key(eventType, targetClass, originClass);
        final var existing = dispatchers.get(key);
        if (existing != null) {
            @SuppressWarnings("unchecked")
            final var casted = (NestedEventDispatcher<E, T, O>) existing;
            return casted;
        } else {
            final var dispatcher = buildNestedDispatcher(eventType, targetClass, originClass);
            dispatchers.put(key, dispatcher);
            return dispatcher;
        }
    }

    @Override
    public <E extends B, O extends ComponentTarget, T extends ComponentTarget>
    StaticEventDispatcher<E, T, O> getStaticEventDispatcher(TypeRef<E> eventType, Class<T> targetClass, Class<O> originClass, T target) {
        final var key = new StaticEventDispatcher.Key(eventType, targetClass, originClass, target);
        final var existing = dispatchers.get(key);
        if (existing != null) {
            @SuppressWarnings("unchecked")
            final var casted = (StaticEventDispatcher<E, T, O>) existing;
            return casted;
        } else {
            final var dispatcher = buildStaticDispatcher(eventType, targetClass, originClass, target);
            dispatchers.put(key, dispatcher);
            return dispatcher;
        }
    }

    @Override
    public Set<EventDispatcher<? extends B, ?>> getEventDispatchers() {
        return Set.copyOf(dispatchers.values());
    }

    protected abstract <E extends B, T extends ComponentTarget>
    EventDispatcher<E, T> buildDispatcher(TypeRef<E> eventType, Class<T> targetClass);

    protected abstract <E extends B, O extends ComponentTarget, T extends ComponentTarget>
    NestedEventDispatcher<E, T, O> buildNestedDispatcher(TypeRef<E> eventType, Class<T> targetClass, Class<O> originClass);

    protected abstract <E extends B, O extends ComponentTarget, T extends ComponentTarget>
    StaticEventDispatcher<E, T, O> buildStaticDispatcher(TypeRef<E> eventType, Class<T> targetClass, Class<O> originClass, T target);

}
