package dev.m00nl1ght.clockwork.event.impl.forwarding;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventForwardingPolicy;
import dev.m00nl1ght.clockwork.event.EventListenerCollection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public class EventForwardingPolicyByLambda<S extends ComponentTarget, D extends ComponentTarget> implements EventForwardingPolicy<S, D> {

    private final TargetType<S> sourceTargetType;
    private final TargetType<D> destinationTargetType;
    private final Function<S, D> targetMapper;

    public EventForwardingPolicyByLambda(@NotNull TargetType<S> sourceTargetType,
                                         @NotNull TargetType<D> destinationTargetType,
                                         @NotNull Function<S, D> targetMapper) {

        this.sourceTargetType = Objects.requireNonNull(sourceTargetType);
        this.destinationTargetType = Objects.requireNonNull(destinationTargetType);
        this.targetMapper = Objects.requireNonNull(targetMapper);
    }

    @Override
    public @NotNull TargetType<S> getSourceTargetType() {
        return sourceTargetType;
    }

    @Override
    public @NotNull TargetType<D> getDestinationTargetType() {
        return destinationTargetType;
    }

    public @NotNull Function<S, D> getTargetMapper() {
        return targetMapper;
    }

    @Override
    public <E extends Event> void bind(@NotNull EventListenerCollection<E, S> source,
                                       @NotNull EventListenerCollection<E, D> destination) {

        // TODO
    }

    @Override
    public <E extends Event> void unbind(@NotNull EventListenerCollection<E, S> source,
                                       @NotNull EventListenerCollection<E, D> destination) {

        // TODO
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventForwardingPolicyByLambda<?, ?> that = (EventForwardingPolicyByLambda<?, ?>) o;
        return sourceTargetType.equals(that.sourceTargetType)
                && destinationTargetType.equals(that.destinationTargetType)
                && targetMapper.equals(that.targetMapper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceTargetType, destinationTargetType, targetMapper);
    }

}
