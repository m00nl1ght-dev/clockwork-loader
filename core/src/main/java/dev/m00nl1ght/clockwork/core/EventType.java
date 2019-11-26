package dev.m00nl1ght.clockwork.core;

public class EventType<E, T extends ComponentTarget> {

    protected final Class<E> eventClass;
    protected final int internalId;

    EventType(Class<E> eventClass, int internalId) {
        this.eventClass = eventClass;
        this.internalId = internalId;
    }

    public void post(T object, E event) {
        object.getTargetType().post(this, object, event);
    }

    public Class<E> getEventClass() {
        return eventClass;
    }

    static class Dummy<E, T extends ComponentTarget> extends EventType<E, T> {

        Dummy(Class<E> eventClass) {
            super(eventClass, -1);
        }

        @Override
        public void post(T target, E event) {}

    }

}
