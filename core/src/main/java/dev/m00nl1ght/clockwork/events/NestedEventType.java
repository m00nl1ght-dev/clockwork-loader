package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.listener.EventListener;

import java.util.List;

public class NestedEventType<E extends Event, T extends ComponentTarget, O extends ComponentTarget> extends EventType<E, T> {

    protected final EventType<E, O> origin;

    public NestedEventType(EventType<E, O> origin, ComponentType<O, T> innerTarget) {
        super(origin.getEventClassType(), innerTarget.getTargetType());
        this.origin = origin;
    }

    @Override
    protected void init() {
        // TODO
    }

    @Override
    public E post(T object, E event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends T> List<EventListener<E, S, ?>> getListeners(TargetType<S> target) {
        return null;
    }

    @Override
    public void addListeners(Iterable<EventListener<E, ? extends T, ?>> eventListeners) {

    }

    @Override
    public void removeListeners(Iterable<EventListener<E, ? extends T, ?>> eventListeners) {

    }

}
