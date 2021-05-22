package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.events.impl.EventTargetKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unchecked")
public final class EventDispatchers {

    // THC: EventTargetKey<E, T> -> EventDispatcher<E, T>
    private final Map<EventTargetKey<?, ?>, EventDispatcher<?, ?>> map = new HashMap<>();
    private final DispatcherFactory factory;

    public EventDispatchers(@NotNull DispatcherFactory factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    public <E extends Event, T extends ComponentTarget>
    @NotNull EventDispatcher<E, T> getDispatcher(@NotNull EventTargetKey<E, T> key) {
        final var existing = getDispatcherOrNull(key);
        if (existing != null) return existing;
        final var collection = Objects.requireNonNull(factory.build(key));
        map.put(key, collection);
        return collection;
    }

    public <E extends Event, T extends ComponentTarget>
    @Nullable EventDispatcher<E, T> getDispatcherOrNull(@NotNull EventTargetKey<E, T> key) {
        return (EventDispatcher<E, T>) map.get(key);
    }

    public @NotNull Set<@NotNull EventDispatcher<?, ?>> getDispatchers() {
        return Set.copyOf(map.values());
    }

    public interface DispatcherFactory {
        <E extends Event, T extends ComponentTarget>
        @NotNull EventDispatcher<E, T> build(@NotNull EventTargetKey<E, T> key);
    }

}
