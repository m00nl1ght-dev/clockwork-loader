package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import org.jetbrains.annotations.NotNull;

public interface EventForwardingPolicy<S extends ComponentTarget, D extends ComponentTarget> {

    @NotNull TargetType<S> getSourceTargetType();

    @NotNull TargetType<D> getDestinationTargetType();

    <E extends Event> void bind(@NotNull EventListenerCollection<E, S> source,
                                @NotNull EventListenerCollection<E, ?> destination);

    <E extends Event> void unbind(@NotNull EventListenerCollection<E, S> source,
                                  @NotNull EventListenerCollection<E, ?> destination);

}
