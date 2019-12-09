package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.debug.profiler.core.EventProfilerGroup;

import java.util.ArrayList;
import java.util.List;

public class EventType<E, T extends ComponentTarget> {

    private final Class<E> eventClass;
    private final TargetType<T> targetType;
    private final int internalId;

    EventType(Class<E> eventClass, TargetType<T> targetType, int internalId) {
        this.eventClass = eventClass;
        this.targetType = targetType;
        this.internalId = internalId;
    }

    @SuppressWarnings("unchecked")
    public E post(T object, E event) {
        final var container = (ComponentContainer<T>) object.getComponentContainer();
        try {
            container.post(this, event);
            return event;
        } catch (Exception e) {
            container.getTargetType().checkCompatibilityForEvent(targetType);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public E post(T object, E event, EventProfilerGroup<T> profilerGroup) {
        final var container = (ComponentContainer<T>) object.getComponentContainer();
        try {
            container.post(this, event, profilerGroup);
            return event;
        } catch (Exception e) {
            container.getTargetType().checkCompatibilityForEvent(targetType);
            throw e;
        }
    }

    @SuppressWarnings({"unchecked", "Convert2streamapi"})
    public List<ComponentType<?, T>> getListeners(TargetType<T> targetType) {
        try {
            final var listeners = targetType.eventListeners[internalId];
            final var list = new ArrayList<ComponentType<?, T>>(listeners.length);
            for (var listener : listeners) list.add(listener.getComponentType());
            return list;
        } catch (Exception e) {
            targetType.checkCompatibilityForEvent(targetType);
            throw e;
        }
    }

    public TargetType<T> getTargetType() {
        return targetType;
    }

    public Class<E> getEventClass() {
        return eventClass;
    }

    public int getInternalId() {
        return internalId;
    }

    static class Empty<E, T extends ComponentTarget> extends EventType<E, T> {

        Empty(Class<E> eventClass, TargetType<T> targetType) {
            super(eventClass, targetType, -1);
        }

        @Override
        public E post(T object, E event) {
            return event;
        }

        @Override
        public E post(T object, E event, EventProfilerGroup<T> profilerGroup) {
            return event;
        }

    }

}
