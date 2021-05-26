package dev.m00nl1ght.clockwork.event.impl.forwarding;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventForwardingPolicy;
import dev.m00nl1ght.clockwork.event.EventListenerCollection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EventForwardingPolicyByComponent<S extends ComponentTarget, D extends ComponentTarget> implements EventForwardingPolicy<S, D> {

    private final TargetType<D> destinationTargetType;
    private final ComponentType<D, S> linkingComponent;

    public EventForwardingPolicyByComponent(@NotNull TargetType<D> destinationTargetType,
                                            @NotNull ComponentType<D, S> linkingComponent) {

        this.destinationTargetType = Objects.requireNonNull(destinationTargetType);
        this.linkingComponent = Objects.requireNonNull(linkingComponent);
    }

    @Override
    public @NotNull TargetType<S> getSourceTargetType() {
        return linkingComponent.getTargetType();
    }

    @Override
    public @NotNull TargetType<D> getDestinationTargetType() {
        return destinationTargetType;
    }

    public @NotNull ComponentType<D, S> getLinkingComponent() {
        return linkingComponent;
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
        EventForwardingPolicyByComponent<?, ?> that = (EventForwardingPolicyByComponent<?, ?>) o;
        return destinationTargetType.equals(that.destinationTargetType)
                && linkingComponent.equals(that.linkingComponent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destinationTargetType, linkingComponent);
    }

}
