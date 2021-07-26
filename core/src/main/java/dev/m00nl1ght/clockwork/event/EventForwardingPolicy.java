package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.TargetType;
import org.jetbrains.annotations.NotNull;

public interface EventForwardingPolicy<B extends Event, S extends ComponentTarget, D extends ComponentTarget> {

    @NotNull TargetType<S> getSourceTargetType();

    @NotNull TargetType<D> getDestinationTargetType();

    @NotNull EventBus<B> getEventBus();

    <E extends B> void bind(@NotNull EventListenerCollection<E, ?> listeners);

    <E extends B> void unbind(@NotNull EventListenerCollection<E, ?> listeners);

}
