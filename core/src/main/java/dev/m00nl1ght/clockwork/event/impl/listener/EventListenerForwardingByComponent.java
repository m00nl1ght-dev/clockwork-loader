package dev.m00nl1ght.clockwork.event.impl.listener;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventListener;

import java.util.Objects;
import java.util.function.BiConsumer;

public class EventListenerForwardingByComponent<E extends Event, T extends ComponentTarget, I extends ComponentTarget, C> extends EventListener<E, T, I> {

    protected final EventListener<E, I, C> innerListener;
    protected final BiConsumer<C, E> innerConsumer;
    protected final int cIdx;

    public EventListenerForwardingByComponent(EventListener<E, I, C> innerListener, ComponentType<I, T> componentType) {
        super(Objects.requireNonNull(innerListener).getEventType(), componentType, innerListener.getPriority());
        this.innerListener = innerListener;
        this.innerConsumer = innerListener.getConsumer();
        this.cIdx = innerListener.getComponentType().getInternalIdx();
    }

    @Override
    public BiConsumer<I, E> getConsumer() {
        return this::invoke;
    }

    private void invoke(I innerTarget, E event) {
        final var container = innerTarget.getComponentContainer();
        @SuppressWarnings("unchecked")
        final C innerComponent = (C) container.getComponent(cIdx);
        if (innerComponent != null) {
            innerConsumer.accept(innerComponent, event);
        }
    }

    public EventListener<E, ? extends I, C> getInnerListener() {
        return innerListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventListenerForwardingByComponent)) return false;
        if (!super.equals(o)) return false;
        EventListenerForwardingByComponent<?, ?, ?, ?> that = (EventListenerForwardingByComponent<?, ?, ?, ?>) o;
        return innerListener.equals(that.innerListener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), innerListener);
    }

}
