package dev.m00nl1ght.clockwork.event.impl.forwarding;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventListener;
import dev.m00nl1ght.clockwork.event.EventListenerCollection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public final class ForwardingObserverByLambda<E extends Event, S extends ComponentTarget, D extends ComponentTarget> implements EventListenerCollection.Observer {

    private final EventListenerCollection<E, ? extends S> source;
    private final EventListenerCollection<E, D> destination;
    private final Function<S, D> mapperFunction;

    public static <E extends Event, S extends ComponentTarget, D extends ComponentTarget> void bind(
            @NotNull EventListenerCollection<E, ? extends S> source,
            @NotNull EventListenerCollection<E, D> destination,
            @NotNull Function<S, D> mapperFunction) {

        source.addObserver(new ForwardingObserverByLambda<>(source, destination, mapperFunction));
    }

    private ForwardingObserverByLambda(
            @NotNull EventListenerCollection<E, ? extends S> source,
            @NotNull EventListenerCollection<E, D> destination,
            @NotNull Function<S, D> mapperFunction) {

        this.source = Objects.requireNonNull(source);
        this.destination = Objects.requireNonNull(destination);
        this.mapperFunction = Objects.requireNonNull(mapperFunction);
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
        //destination.add(new ForwardingEventListener<>(casted, destination.getTargetType(), mapperFunction)); TODO
    }

    @Override
    public void onRemoved(EventListenerCollection<?, ?> collection, EventListener<?, ?, ?> listener) {
        @SuppressWarnings("unchecked")
        final var casted = (EventListener<E, S, ?>) listener;
        //destination.remove(new ForwardingEventListener<>(casted, destination.getTargetType(), mapperFunction)); TODO
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForwardingObserverByLambda)) return false;
        ForwardingObserverByLambda<?, ?, ?> that = (ForwardingObserverByLambda<?, ?, ?>) o;
        return source == that.source && destination == that.destination;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination);
    }

}
