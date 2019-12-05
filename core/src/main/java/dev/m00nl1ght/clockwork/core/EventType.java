package dev.m00nl1ght.clockwork.core;

public class EventType<E, T extends ComponentTarget> {

    private final Class<E> eventClass;
    private final TargetType<?> rootTarget;
    private final int internalId;

    EventType(Class<E> eventClass, TargetType<?> rootTarget, int internalId) {
        this.eventClass = eventClass;
        this.rootTarget = rootTarget;
        this.internalId = internalId;
    }

    @SuppressWarnings("unchecked")
    public void post(T object, E event) {
        final var container = (ComponentContainer<T>) object.getComponentContainer();
        try {
            container.post(this, event);
        } catch (Exception e) {
            container.getTargetType().checkCompatibilityForEvent(rootTarget);
            throw e;
        }
    }

    public TargetType<?> getRootTarget() {
        return rootTarget;
    }

    public Class<E> getEventClass() {
        return eventClass;
    }

    public int getInternalId() {
        return internalId;
    }

    static class Empty<E, T extends ComponentTarget> extends EventType<E, T> {

        Empty(Class<E> eventClass, TargetType<?> rootTarget) {
            super(eventClass, rootTarget, -1);
        }

        @Override
        public void post(T object, E event) {}

    }

}
