package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.AbstractNestedEventDispatcher;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventDispatcher;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.NestedEventListener;

import java.util.List;
import java.util.stream.Collectors;

public class NestedEventDispatcherImpl<E extends Event, T extends ComponentTarget, O extends ComponentTarget> extends AbstractNestedEventDispatcher<E, T, O> {

    public NestedEventDispatcherImpl(EventDispatcher<E, O> origin, ComponentType<T, O> componentOrigin, TargetType<T> targetType) {
        super(origin, componentOrigin, targetType);
    }

    @Override
    protected List getRawListeners(TargetType<?> target) {
        if (target != targetType) checkCompatibility(target);
        return origin.getListeners(componentOrigin.getTargetType()).stream()
                .filter(l -> l instanceof NestedEventListener)
                .map(l -> (NestedEventListener) l)
                .filter(l -> l.getComponentType() == componentOrigin)
                .map(NestedEventListener::getInnerListener)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    protected <I> NestedEventListener<E, O, T, I> buildNestedListener(EventListener<E, ? extends T, I> listener) {
        final var target = listener.getComponentType().getTargetType();
        if (target != targetType) checkCompatibility(target);
        return new NestedEventListener<>(listener, componentOrigin);
    }

}
