package dev.m00nl1ght.clockwork.events.listener;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.events.Event;

import java.util.Objects;
import java.util.function.BiConsumer;

public class NestedEventListener<E extends Event, T extends ComponentTarget, C extends ComponentTarget, I> extends EventListener<E, T, C> {

    protected final EventListener<E, ? extends C, I> innerListener;
    protected final BiConsumer<I, E> innerConsumer;
    protected final int cIdx;

    public NestedEventListener(EventListener<E, ? extends C, I> innerListener, ComponentType<C, T> componentType) {
        super(Objects.requireNonNull(innerListener).getEventType(), componentType, innerListener.getPriority());
        this.innerListener = innerListener;
        this.innerConsumer = innerListener.getConsumer();
        this.cIdx = innerListener.getComponentType().getInternalIdx();
    }

    @Override
    public BiConsumer<C, E> getConsumer() {
        return this::invoke;
    }

    private void invoke(C innerTarget, E event) {
        @SuppressWarnings("unchecked")
        final I innerComponent = (I) innerTarget.getComponent(cIdx);
        if (innerComponent != null) {
            innerConsumer.accept(innerComponent, event);
        }
    }

    public EventListener<E, ? extends C, I> getInnerListener() {
        return innerListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NestedEventListener)) return false;
        if (!super.equals(o)) return false;
        NestedEventListener<?, ?, ?, ?> that = (NestedEventListener<?, ?, ?, ?>) o;
        return innerListener.equals(that.innerListener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), innerListener);
    }

}
