package dev.m00nl1ght.clockwork.events.listener;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.events.Event;

import java.util.Objects;
import java.util.function.BiConsumer;

public class ForwardingEventListener<E extends Event, T extends ComponentTarget, I extends ComponentTarget, C> extends EventListener<E, T, I> {

    protected final EventListener<E, ? extends I, C> innerListener;
    protected final BiConsumer<C, E> innerConsumer;
    protected final int cIdx, tIdxF, tIdxL;

    public ForwardingEventListener(EventListener<E, ? extends I, C> innerListener, ComponentType<I, T> componentType) {
        super(Objects.requireNonNull(innerListener).getEventType(), componentType, innerListener.getPriority());
        this.innerListener = innerListener;
        this.innerConsumer = innerListener.getConsumer();
        this.cIdx = innerListener.getComponentType().getInternalIdx();
        this.tIdxF = innerListener.getComponentType().getTargetType().getSubtargetIdxFirst();
        this.tIdxL = innerListener.getComponentType().getTargetType().getSubtargetIdxLast();
    }

    @Override
    public BiConsumer<I, E> getConsumer() {
        if (innerListener.getComponentType().getTargetType().getTargetClass() == componentType.getComponentClass()) {
            return this::invokeExact;
        } else {
            return this::invoke;
        }
    }

    private void invokeExact(I innerTarget, E event) {
        @SuppressWarnings("unchecked")
        final C innerComponent = (C) innerTarget.getComponent(cIdx);
        if (innerComponent != null) {
            innerConsumer.accept(innerComponent, event);
        }
    }

    private void invoke(I innerTarget, E event) {
        final var tIdx = innerTarget.getTargetType().getSubtargetIdxFirst();
        if (tIdx >= tIdxF && tIdx <= tIdxL) {
            @SuppressWarnings("unchecked")
            final C innerComponent = (C) innerTarget.getComponent(cIdx);
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
        if (!(o instanceof ForwardingEventListener)) return false;
        if (!super.equals(o)) return false;
        ForwardingEventListener<?, ?, ?, ?> that = (ForwardingEventListener<?, ?, ?, ?>) o;
        return innerListener.equals(that.innerListener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), innerListener);
    }

}
