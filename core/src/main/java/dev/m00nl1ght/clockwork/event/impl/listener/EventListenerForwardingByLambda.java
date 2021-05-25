package dev.m00nl1ght.clockwork.event.impl.listener;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventListener;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class EventListenerForwardingByLambda<E extends Event, T extends ComponentTarget, I extends ComponentTarget, C> extends EventListener<E, T, T> {

    protected final EventListener<E, ? extends I, C> innerListener;
    protected final Function<T, I> targetFunction;
    protected final BiConsumer<C, E> innerConsumer;
    protected final int cIdx, tIdxF, tIdxL;

    public EventListenerForwardingByLambda(EventListener<E, ? extends I, C> innerListener, TargetType<T> targetType, Function<T, I> targetFunction) {
        super(Objects.requireNonNull(innerListener).getEventType(), targetType.getIdentityComponentType(), innerListener.getPriority());
        this.innerListener = innerListener;
        this.innerConsumer = innerListener.getConsumer();
        this.cIdx = innerListener.getComponentType().getInternalIdx();
        this.tIdxF = innerListener.getComponentType().getTargetType().getSubtargetIdxFirst();
        this.tIdxL = innerListener.getComponentType().getTargetType().getSubtargetIdxLast();
        this.targetFunction = Objects.requireNonNull(targetFunction);
    }

    @Override
    public BiConsumer<T, E> getConsumer() {
        return this::invoke;
    }

    private void invoke(T target, E event) {
        final var innerTarget = targetFunction.apply(target);
        if (innerTarget == null) return;
        final var container = innerTarget.getComponentContainer();
        final var tIdx = container.getTargetType().getSubtargetIdxFirst();
        if (tIdx >= tIdxF && tIdx <= tIdxL) {
            @SuppressWarnings("unchecked")
            final C innerComponent = (C) container.getComponent(cIdx);
            if (innerComponent != null) {
                innerConsumer.accept(innerComponent, event);
            }
        }
    }

    public EventListener<E, ? extends I, C> getInnerListener() {
        return innerListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventListenerForwardingByLambda)) return false;
        if (!super.equals(o)) return false;
        EventListenerForwardingByLambda<?, ?, ?, ?> that = (EventListenerForwardingByLambda<?, ?, ?, ?>) o;
        return innerListener.equals(that.innerListener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), innerListener);
    }

}
