package dev.m00nl1ght.clockwork.event.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.event.EventListenerCollection;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    public <T extends ComponentTarget>
    @NotNull Set<@NotNull EventListenerCollection<?, T>> getCollections(TargetType<T> targetType) {
        return map.values().stream()
                .filter(lc -> lc.getTargetType() == targetType)
                .map(lc -> (EventListenerCollection<?, T>) lc)
                .collect(Collectors.toUnmodifiableSet());
    }

    public <E extends Event>
    @NotNull Set<@NotNull EventListenerCollection<E, ?>> getCollections(TypeRef<E> eventType) {
        return map.values().stream()
                .filter(lc -> lc.getEventType().equals(eventType))
                .map(lc -> (EventListenerCollection<E, ?>) lc)
                .collect(Collectors.toUnmodifiableSet());
    }

    public interface CollectionFactory {
        <E extends Event, T extends ComponentTarget>
        @NotNull EventListenerCollection<E, T> build(@NotNull EventTargetKey<E, T> key);
    }

}
