package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventListenerCollection;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.ForwardingEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ForwardingObserver<E extends Event, S extends ComponentTarget, D extends ComponentTarget> implements EventListenerCollection.Observer {

    private final EventListenerCollection<E, ? extends S> source;
    private final EventListenerCollection<E, D> destination;
    private final ComponentType<S, D> bindingComponent;

    public static <E extends Event, S extends ComponentTarget, D extends ComponentTarget> void bind(
            @NotNull EventListenerCollection<E, ? extends S> source,
            @NotNull EventListenerCollection<E, D> destination,
            @NotNull ComponentType<S, D> bindingComponent) {

        source.addObserver(new ForwardingObserver<>(source, destination, bindingComponent));
    }

    private ForwardingObserver(
            @NotNull EventListenerCollection<E, ? extends S> source,
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
        destination.add(new ForwardingEventListener<>(casted, bindingComponent));
    }

    @Override
    public void onRemoved(EventListenerCollection<?, ?> collection, EventListener<?, ?, ?> listener) {
        @SuppressWarnings("unchecked")
        final var casted = (EventListener<E, S, ?>) listener;
        destination.remove(new ForwardingEventListener<>(casted, bindingComponent));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForwardingObserver)) return false;
        ForwardingObserver<?, ?, ?> that = (ForwardingObserver<?, ?, ?>) o;
        return source == that.source && destination == that.destination;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination);
    }

}
