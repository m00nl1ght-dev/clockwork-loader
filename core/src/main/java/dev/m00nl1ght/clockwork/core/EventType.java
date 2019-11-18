package dev.m00nl1ght.clockwork.core;

public class EventType<E, T extends ComponentTarget> {

    private final ComponentTargetType<T> target;
    private final Class<E> eventClass;
    private final int internalId;

    EventType(ComponentTargetType<T> target, Class<E> eventClass, int internalId) {
        this.target = target;
        this.eventClass = eventClass;
        this.internalId = internalId;
    }

    @SuppressWarnings("unchecked")
    public void post(T object, E event) {
        // TODO check object.getTargetType() against target and parents (?)
        object.getTargetType().events[internalId].post(object, event);
    }

    static class Dummy<E, T extends ComponentTarget> extends EventType<E, T> {

        Dummy(ComponentTargetType<T> target, Class<E> eventClass) {
            super(target, eventClass, -1);
        }

        @Override
        public void post(T target, E event) {}

    }

}
