package dev.m00nl1ght.clockwork.events.listener;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.Event;

import java.util.Objects;
import java.util.function.BiConsumer;

public class StaticEventListener<E extends Event, T extends ComponentTarget, I extends ComponentTarget, C> extends EventListener<E, T, T> {

    protected final EventListener<E, I, C> innerListener;
    protected final BiConsumer<C, E> innerConsumer;
    protected final I innerTarget;
    protected C innerComponent;

    public StaticEventListener(EventListener<E, I, C> innerListener, TargetType<T> targetType, I innerTarget) {
        super(Objects.requireNonNull(innerListener).getEventType(),
                targetType.getIdentityComponentType(), innerListener.getPriority());
        this.innerTarget = Objects.requireNonNull(innerTarget);
        this.innerListener = innerListener;
        this.innerConsumer = innerListener.getConsumer();
        this.innerComponent = innerListener.getComponentType().get(innerTarget);
    }

    @Override
    public BiConsumer<T, E> getConsumer() {
        return this::invoke;
    }

    private void invoke(T object, E event) {
        if (innerComponent != null) {
            innerConsumer.accept(innerComponent, event);
        } else { // TODO rework?
            innerComponent = innerListener.getComponentType().get(innerTarget);
            if (innerComponent != null) {
                innerConsumer.accept(innerComponent, event);
            }
        }
    }

    public EventListener<E, I, C> getInnerListener() {
        return innerListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StaticEventListener)) return false;
        if (!super.equals(o)) return false;
        StaticEventListener<?, ?, ?, ?> that = (StaticEventListener<?, ?, ?, ?>) o;
        return innerListener.equals(that.innerListener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), innerListener);
    }

}
