package dev.m00nl1ght.clockwork.event.impl.forwarding;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventBus;
import dev.m00nl1ght.clockwork.event.EventListener;
import dev.m00nl1ght.clockwork.event.EventListenerCollection;
import dev.m00nl1ght.clockwork.event.impl.listener.EventListenerForwardingByComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ForwardingObserverByComponent<B extends Event, S extends ComponentTarget, D extends ComponentTarget> implements EventListenerCollection.Observer<B> {

    private final TargetType<D> destinationTarget;
    private final ComponentType<D, S> linkingComponent;
    private final EventBus<B> eventBus;

    public ForwardingObserverByComponent(
            @NotNull TargetType<D> destinationTarget,
            @NotNull ComponentType<D, S> linkingComponent,
            @NotNull EventBus<B> eventBus) {

        this.destinationTarget = Objects.requireNonNull(destinationTarget);
        this.linkingComponent = Objects.requireNonNull(linkingComponent);
        this.eventBus = Objects.requireNonNull(eventBus);
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForwardingObserverByComponent)) return false;
        ForwardingObserverByComponent<?, ?, ?> that = (ForwardingObserverByComponent<?, ?, ?>) o;
        return destinationTarget == that.destinationTarget
                && linkingComponent == that.linkingComponent
                && eventBus == that.eventBus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(destinationTarget, linkingComponent, eventBus);
    }

}
