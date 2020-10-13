package dev.m00nl1ght.clockwork.events.listener;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.util.Objects;
import java.util.function.BiConsumer;

public class StaticEventListener<E extends Event, T extends ComponentTarget, I extends ComponentTarget, C> extends EventListener<E, T, T> {

    protected final EventListener<E, I, C> innerListener;
    protected final BiConsumer<C, E> innerConsumer;
    protected final C innerComponent;

    public StaticEventListener(EventListener<E, I, C> innerListener, TargetType<T> targetType, I target) {
        super(Arguments.notNull(innerListener, "innerListener").getEventType(),
                targetType.getIdentityComponentType(), innerListener.getPriority());
        this.innerListener = innerListener;
        this.innerConsumer = innerListener.getConsumer();
        this.innerComponent = innerListener.getComponentType().get(target);
        if (innerComponent == null)
            throw FormatUtil.rtExc("Component [] is not initialised", innerListener.getComponentType());
    }

    @Override
    public BiConsumer<T, E> getConsumer() {
        return this::invoke;
    }

    private void invoke(T object, E event) {
        innerConsumer.accept(innerComponent, event);
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
