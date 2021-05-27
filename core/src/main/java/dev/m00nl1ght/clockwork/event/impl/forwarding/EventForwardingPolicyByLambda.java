package dev.m00nl1ght.clockwork.event.impl.forwarding;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.event.*;
import dev.m00nl1ght.clockwork.event.impl.listener.EventListenerForwardingByLambda;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public class EventForwardingPolicyByLambda<B extends Event, S extends ComponentTarget, D extends ComponentTarget> implements EventForwardingPolicy<B, S, D> {

    private final TargetType<S> sourceTarget;
    private final TargetType<D> destinationTarget;
    private final Function<S, D> targetMapper;
    private final EventBus<B> eventBus;

    private final Observer observer = new Observer();

    public EventForwardingPolicyByLambda(
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
    public @NotNull TargetType<S> getSourceTargetType() {
        return sourceTarget;
    }

    @Override
    public @NotNull TargetType<D> getDestinationTargetType() {
        return destinationTarget;
    }

    @Override
    public @NotNull EventBus<B> getEventBus() {
        return eventBus;
    }

    public @NotNull Function<S, D> getTargetMapper() {
        return targetMapper;
    }

    @Override
    public <E extends Event> void bind(@NotNull EventListenerCollection<E, S> source,
                                       @NotNull EventListenerCollection<E, ?> destination) {

        // TODO
    }

    @Override
    public <E extends Event> void unbind(@NotNull EventListenerCollection<E, S> source,
                                         @NotNull EventListenerCollection<E, ?> destination) {

        // TODO
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final var that = (EventForwardingPolicyByLambda<?, ?, ?>) o;
        return sourceTarget.equals(that.sourceTarget)
                && destinationTarget.equals(that.destinationTarget)
                && targetMapper.equals(that.targetMapper)
                && eventBus.equals(that.eventBus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceTarget, destinationTarget, targetMapper, eventBus);
    }

    private class Observer implements EventListenerCollection.Observer<B> {

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

    }

}
