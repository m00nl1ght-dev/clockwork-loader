package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.NestedEventListener;
import dev.m00nl1ght.clockwork.util.Arguments;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class NestedEventType<E extends Event, T extends ComponentTarget, O extends ComponentTarget> extends EventType<E, T> {

    protected final EventType<E, O> origin;
    protected final ComponentType<T, O> componentOrigin;
    protected final TargetType<T> targetType;

    public NestedEventType(EventType<E, O> origin, ComponentType<T, O> componentOrigin, TargetType<T> targetType) {
        super(Arguments.notNull(origin, "origin").getEventClassType(), Arguments.notNull(targetType, "targetType"));
        this.origin = origin;
        this.componentOrigin = componentOrigin;
        this.targetType = targetType;
    }

    @Override
    public E post(T object, E event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends T> List<EventListener<E, S, ?>> getListeners(TargetType<S> target) {
        return null; // TODO
    }

    @Override
    public void addListeners(Collection<EventListener<E, ? extends T, ?>> eventListeners) {
        origin.addListeners(eventListeners.stream()
                .map(l -> new NestedEventListener<>(l, componentOrigin))
                .collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public void removeListeners(Collection<EventListener<E, ? extends T, ?>> eventListeners) {
        // TODO
    }

}
