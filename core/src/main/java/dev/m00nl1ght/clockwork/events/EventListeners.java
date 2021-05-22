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
public final class EventListeners {

    // THC: EventTargetKey<E, T> -> EventListenerCollection<E, T>
    private final Map<EventTargetKey<?, ?>, EventListenerCollection<?, ?>> map = new HashMap<>();
    private final CollectionFactory factory;

    public EventListeners(@NotNull CollectionFactory factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    public <E extends Event, T extends ComponentTarget>
    @NotNull EventListenerCollection<E, T> getCollection(@NotNull EventTargetKey<E, T> key) {
        final var existing = getCollectionOrNull(key);
        if (existing != null) return existing;
        final var collection = Objects.requireNonNull(factory.build(key));
        map.put(key, collection);
        return collection;
    }

    public <E extends Event, T extends ComponentTarget>
    @Nullable EventListenerCollection<E, T> getCollectionOrNull(@NotNull EventTargetKey<E, T> key) {
        return (EventListenerCollection<E, T>) map.get(key);
    }

    public @NotNull Set<@NotNull EventListenerCollection<?, ?>> getCollections() {
        return Set.copyOf(map.values());
    }

    public interface CollectionFactory {
        <E extends Event, T extends ComponentTarget>
        @NotNull EventListenerCollection<E, T> build(@NotNull EventTargetKey<E, T> key);
    }

}
