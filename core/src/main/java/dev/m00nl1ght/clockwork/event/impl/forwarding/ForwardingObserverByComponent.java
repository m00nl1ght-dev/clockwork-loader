package dev.m00nl1ght.clockwork.event.impl.forwarding;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventListenerCollection;
import dev.m00nl1ght.clockwork.event.EventListener;
import dev.m00nl1ght.clockwork.event.impl.listener.EventListenerForwardingByComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ForwardingObserverByComponent<E extends Event, S extends ComponentTarget, D extends ComponentTarget> implements EventListenerCollection.Observer {

    private final EventListenerCollection<E, S> source;
    private final EventListenerCollection<E, D> destination;
    private final ComponentType<S, D> bindingComponent;

    public static <E extends Event, S extends ComponentTarget, D extends ComponentTarget> void bind(
            @NotNull EventListenerCollection<E, S> source,
            @NotNull EventListenerCollection<E, D> destination,
            @NotNull ComponentType<S, D> bindingComponent) {

        source.addObserver(new ForwardingObserverByComponent<>(source, destination, bindingComponent));
    }

    private ForwardingObserverByComponent(
            @NotNull EventListenerCollection<E, S> source,
            @NotNull EventListenerCollection<E, D> destination,
            @NotNull ComponentType<S, D> bindingComponent) {

        this.source = Objects.requireNonNull(source);
        this.destination = Objects.requireNonNull(destination);
        this.bindingComponent = Objects.requireNonNull(bindingComponent);
        source.get().forEach(l -> onAdded(source, l));
    }

    @Override
    public void onChange(EventListenerCollection<?, ?> collection) {
        // NO-OP
    }

    @Override
    public void onAdded(EventListenerCollection<?, ?> collection, EventListener<?, ?, ?> listener) {
        @SuppressWarnings("unchecked")
        final var casted = (EventListener<E, S, ?>) listener;
        destination.add(new EventListenerForwardingByComponent<>(casted, bindingComponent));
    }

    @Override
    public void onRemoved(EventListenerCollection<?, ?> collection, EventListener<?, ?, ?> listener) {
        @SuppressWarnings("unchecked")
        final var casted = (EventListener<E, S, ?>) listener;
        destination.remove(new EventListenerForwardingByComponent<>(casted, bindingComponent));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForwardingObserverByComponent)) return false;
        ForwardingObserverByComponent<?, ?, ?> that = (ForwardingObserverByComponent<?, ?, ?>) o;
        return source == that.source && destination == that.destination;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination);
    }

}
