package dev.m00nl1ght.clockwork.core;

public class EventType<E, T extends ComponentTarget> {

    private final Class<E> eventClass;
    private final TargetType<T> targetType;
    private final int internalId;

    EventType(Class<E> eventClass, TargetType<T> targetType, int internalId) {
        this.eventClass = eventClass;
        this.targetType = targetType;
        this.internalId = internalId;
    }

    public void post(T object, E event) {
        try {
            object.getTargetType().post(internalId, object, event);
        } catch (Exception e) {
            this.targetType.checkCompatibility(object);
            throw e;
        }
    }

    public TargetType<T> getTargetType() {
        return targetType;
    }

    public Class<E> getEventClass() {
        return eventClass;
    }

}
