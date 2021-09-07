package dev.m00nl1ght.clockwork.event.impl;

import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.event.debug.EventDispatcherProfilerGroup;
import dev.m00nl1ght.clockwork.event.EventListenerCollection;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class CompiledListeners {

    public static final CompiledListeners EMPTY = new CompiledListeners(new EventListener[]{}, null);

    public final EventListener[] listeners;
    public final BiConsumer[] consumers;
    public final int[] cIdxs;

    @SuppressWarnings("unchecked")
    private CompiledListeners(@NotNull EventListener[] listeners,
                              @Nullable EventDispatcherProfilerGroup profilerGroup) {
        this.listeners = Objects.requireNonNull(listeners);
        this.cIdxs = new int[listeners.length];
        this.consumers = new BiConsumer[listeners.length];
        if (profilerGroup != null) {
            for (int i = 0; i < listeners.length; i++) {
                final var listener = listeners[i];
                this.consumers[i] = profilerGroup.getEntry(listener);
                this.cIdxs[i] = listener.getComponentType().getInternalIdx();
            }
        } else {
            for (int i = 0; i < listeners.length; i++) {
                final var listener = listeners[i];
                this.consumers[i] = listener.getConsumer();
                this.cIdxs[i] = listener.getComponentType().getInternalIdx();
            }
        }
    }

    @NotNull
    public static <T extends ComponentTarget, E extends Event>
    CompiledListeners build(@Nullable CompiledListeners inherited,
                            @Nullable EventListenerCollection<E, T> collection,
                            @Nullable EventDispatcherProfilerGroup<E, T> profilerGroup) {
        final var emptyCollection = collection == null || collection.get().isEmpty();
        if (inherited == null) {
            if (emptyCollection) return EMPTY;
            return new CompiledListeners(collection.get().toArray(EventListener[]::new), profilerGroup);
        } else if (emptyCollection) {
            if (profilerGroup == null) return inherited;
            return new CompiledListeners(inherited.listeners, profilerGroup);
        } else {
            return new CompiledListeners(Stream.concat(Arrays.stream(inherited.listeners),
                    collection.get().stream()).toArray(EventListener[]::new), profilerGroup);
        }
    }

}
