package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.AbstractStaticEventDispatcher;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventDispatcher;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.StaticEventListener;

import java.util.List;
import java.util.stream.Collectors;

public class StaticEventDispatcherImpl<E extends Event, T extends ComponentTarget, O extends ComponentTarget> extends AbstractStaticEventDispatcher<E, T, O> {

    public StaticEventDispatcherImpl(EventDispatcher<E, O> origin, T target) {
        super(origin, target);
    }

    @Override
    protected List getRawListeners(TargetType<?> target) {
        if (target != targetType) checkCompatibility(target);
        return origin.getListeners(originIdentity.getTargetType()).stream()
                .filter(l -> l instanceof StaticEventListener)
                .map(l -> (StaticEventListener) l)
                .filter(l -> l.getComponentType() == originIdentity)
                .map(StaticEventListener::getInnerListener)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    protected <C> StaticEventListener<E, O, T, C> buildStaticListener(EventListener<E, ? extends T, C> listener) {
        final var target = listener.getComponentType().getTargetType();
        if (target != targetType) checkCompatibility(target);
        @SuppressWarnings("unchecked")
        final var casted = (EventListener<E, T, C>) listener;
        return new StaticEventListener<>(casted, originIdentity.getTargetType(), this.target);
    }

}
