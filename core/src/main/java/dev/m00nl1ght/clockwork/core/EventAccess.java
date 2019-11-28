package dev.m00nl1ght.clockwork.core;

// TODO find better solution
public class EventAccess<E, T extends ComponentTarget> {

    private final Class<E> eventClass;
    private TargetType<T> targetType;
    private int internalId = -1;

    EventAccess(Class<E> eventClass) {
        this.eventClass = eventClass;
    }

    EventAccess(Class<E> eventClass, TargetType<T> targetType, int internalId) {
        this(eventClass);
        init(targetType, internalId);
    }

    void init(TargetType<T> targetType, int internalId) {
        if (this.targetType != null) throw new IllegalStateException();
        this.targetType = targetType;
        this.internalId = internalId;
    }

    public void post(T object, E event) {
        try {
            if (internalId >= 0) object.getTargetType().post(internalId, object, event);
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
