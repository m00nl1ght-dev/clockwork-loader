package dev.m00nl1ght.clockwork.event.impl.forwarding;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.event.*;
import dev.m00nl1ght.clockwork.event.impl.listener.EventListenerForwardingByComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EventForwardingPolicyByComponent<B extends Event, S extends ComponentTarget, D extends ComponentTarget> implements EventForwardingPolicy<B, S, D> {

    private final TargetType<D> destinationTarget;
    private final ComponentType<D, S> linkingComponent;
    private final EventBus<B> eventBus;

    private final Observer observer = new Observer();

    public EventForwardingPolicyByComponent(@NotNull TargetType<D> destinationTarget,
                                            @NotNull ComponentType<D, S> linkingComponent,
                                            @NotNull EventBus<B> eventBus) {

        this.destinationTarget = Objects.requireNonNull(destinationTarget);
        this.linkingComponent = Objects.requireNonNull(linkingComponent);
        this.eventBus = Objects.requireNonNull(eventBus);
    }

    @Override
    public @NotNull TargetType<S> getSourceTargetType() {
        return linkingComponent.getTargetType();
    }

    @Override
    public @NotNull TargetType<D> getDestinationTargetType() {
        return destinationTarget;
    }

    @Override
    public @NotNull EventBus<B> getEventBus() {
        return eventBus;
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
        final var that = (EventForwardingPolicyByComponent<?, ?, ?>) o;
        return destinationTarget.equals(that.destinationTarget)
                && linkingComponent.equals(that.linkingComponent)
                && eventBus.equals(that.eventBus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destinationTarget, linkingComponent, eventBus);
    }

    private class Observer implements EventListenerCollection.Observer<B> {

        @Override
        public <E extends B, T extends ComponentTarget, C>
        void onAdded(EventListenerCollection<E, T> collection, EventListener<E, T, C> listener) {
            @SuppressWarnings("unchecked")
            final var castedListener = (EventListener<E, D, C>) listener;
            final var source = eventBus.getListenerCollection(listener.getEventType(), linkingComponent.getTargetType());
            source.add(new EventListenerForwardingByComponent<>(castedListener, linkingComponent));
        }

        @Override
        public <E extends B, T extends ComponentTarget, C>
        void onRemoved(EventListenerCollection<E, T> collection, EventListener<E, T, C> listener) {
            @SuppressWarnings("unchecked")
            final var castedListener = (EventListener<E, D, C>) listener;
            final var source = eventBus.getListenerCollection(listener.getEventType(), linkingComponent.getTargetType());
            source.remove(new EventListenerForwardingByComponent<>(castedListener, linkingComponent));
        }

    }

}
