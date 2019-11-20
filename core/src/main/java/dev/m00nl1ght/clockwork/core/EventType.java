package dev.m00nl1ght.clockwork.core;

public class EventType<E, T extends ComponentTarget<? super T>> {

    private final TargetType<T> target;
    private final Class<E> eventClass;
    private final int internalId;

    EventType(TargetType<T> target, Class<E> eventClass, int internalId) {
        this.target = target;
        this.eventClass = eventClass;
        this.internalId = internalId;
    }

    @SuppressWarnings("unchecked")
    public void post(T object, E event) {
        if (object.getTargetType().getRoot() != target.getRoot()) throw new IllegalArgumentException();
        object.getTargetType().events[internalId].post(object, event);
    }

    protected int getInternalId() {
        return internalId;
    }

    static class Dummy<E, T extends ComponentTarget<? super T>> extends EventType<E, T> {

        Dummy(TargetType<T> target, Class<E> eventClass) {
            super(target, eventClass, -1);
        }

        @Override
        public void post(T target, E event) {}

    }

}
