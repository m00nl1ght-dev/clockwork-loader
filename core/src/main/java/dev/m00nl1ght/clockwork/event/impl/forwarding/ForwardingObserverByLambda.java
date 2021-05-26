package dev.m00nl1ght.clockwork.event.impl.forwarding;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventBus;
import dev.m00nl1ght.clockwork.event.EventListener;
import dev.m00nl1ght.clockwork.event.EventListenerCollection;
import dev.m00nl1ght.clockwork.event.impl.listener.EventListenerForwardingByLambda;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public final class ForwardingObserverByLambda<B extends Event, S extends ComponentTarget, D extends ComponentTarget> implements EventListenerCollection.Observer<B> {

    private final TargetType<S> sourceTarget;
    private final TargetType<D> destinationTarget;
    private final Function<S, D> targetMapper;
    private final EventBus<B> eventBus;

    public ForwardingObserverByLambda(
            @NotNull TargetType<S> sourceTarget,
            @NotNull TargetType<D> destinationTarget,
            @NotNull Function<S, D> targetMapper,
            @NotNull EventBus<B> eventBus) {

        this.sourceTarget = Objects.requireNonNull(sourceTarget);
        this.destinationTarget = Objects.requireNonNull(destinationTarget);
        this.targetMapper = Objects.requireNonNull(targetMapper);
        this.eventBus = Objects.requireNonNull(eventBus);
    }

    @Override
    public <E extends B, T extends ComponentTarget, C>
    void onAdded(EventListenerCollection<E, T> collection, EventListener<E, T, C> listener) {
        final var source = eventBus.getListenerCollection(listener.getEventType(), sourceTarget);
        source.add(new EventListenerForwardingByLambda<>(listener, sourceTarget, destinationTarget, targetMapper));
    }

    @Override
    public <E extends B, T extends ComponentTarget, C>
    void onRemoved(EventListenerCollection<E, T> collection, EventListener<E, T, C> listener) {
        final var source = eventBus.getListenerCollection(listener.getEventType(), sourceTarget);
        source.remove(new EventListenerForwardingByLambda<>(listener, sourceTarget, destinationTarget, targetMapper));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForwardingObserverByLambda)) return false;
        final var that = (ForwardingObserverByLambda<?, ?, ?>) o;
        return sourceTarget == that.sourceTarget
                && destinationTarget == that.destinationTarget
                && targetMapper.equals(that.targetMapper)
                && eventBus == that.eventBus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceTarget, destinationTarget, targetMapper, eventBus);
    }

}
