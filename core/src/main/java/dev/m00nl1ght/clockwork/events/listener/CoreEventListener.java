package dev.m00nl1ght.clockwork.events.listener;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.util.Arguments;

import java.util.Objects;
import java.util.function.BiConsumer;

public class CoreEventListener<E extends Event, T extends ComponentTarget, I> extends EventListener<E, T, T> {

    protected final EventListener<E, ClockworkCore, I> innerListener;
    protected final BiConsumer<I, E> innerConsumer;
    protected final I innerComponent;

    public CoreEventListener(EventListener<E, ClockworkCore, I> innerListener, TargetType<T> target, ClockworkCore core) {
        super(Arguments.notNull(innerListener, "innerListener").getEventClassType(),
                target.getIdentityComponentType(), innerListener.getPriority());
        this.innerListener = innerListener;
        this.innerConsumer = innerListener.getConsumer();
        core.getState().requireOrAfter(ClockworkCore.State.INITIALISED);
        this.innerComponent = innerListener.getComponentType().get(core);
    }

    @Override
    public BiConsumer<T, E> getConsumer() {
        return this::invoke;
    }

    private void invoke(T object, E event) {
        innerConsumer.accept(innerComponent, event);
    }

    public EventListener<E, ClockworkCore, I> getInnerListener() {
        return innerListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CoreEventListener)) return false;
        if (!super.equals(o)) return false;
        CoreEventListener<?, ?, ?> that = (CoreEventListener<?, ?, ?>) o;
        return innerListener.equals(that.innerListener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), innerListener);
    }

}
