package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.NestedEventListener;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;

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
    @SuppressWarnings("unchecked")
    public <S extends T> List<EventListener<E, S, ?>> getListeners(TargetType<S> target) {
        if (target != targetType) checkCompatibility(target);
        return origin.getListeners(componentOrigin.getTargetType()).stream()
                .filter(l -> l instanceof NestedEventListener)
                .map(l -> (NestedEventListener) l)
                .filter(l -> l.getComponentType() == componentOrigin)
                .map(l -> (EventListener<E, S, ?>) l.getInnerListener())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void addListeners(Collection<EventListener<E, ? extends T, ?>> eventListeners) {
        origin.addListeners(eventListeners.stream()
                .map(this::buildNestedListener)
                .collect(Collectors.toUnmodifiableList()));
    }

    private <I> NestedEventListener<E, O, T, I> buildNestedListener(EventListener<E, ? extends T, I> listener) {
        final var target = listener.getComponentType().getTargetType();
        if (target != targetType) checkCompatibility(target);
        return new NestedEventListener<>(listener, componentOrigin);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeListeners(Collection<EventListener<E, ? extends T, ?>> eventListeners) {
        origin.removeListeners(origin.getListeners(componentOrigin.getTargetType()).stream()
                .filter(l -> l instanceof NestedEventListener).map(l -> (NestedEventListener) l)
                .filter(l -> l.getComponentType() == componentOrigin && eventListeners.contains(l.getInnerListener()))
                .collect(Collectors.toUnmodifiableList()));
    }

    @Override
    protected void checkCompatibility(TargetType<?> otherType) {
        if (otherType != targetType) {
            final var msg = "Cannot post event [] to different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, this, otherType));
        }
    }

}
