package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.EventBusProfilerGroup;
import dev.m00nl1ght.clockwork.debug.profiler.Profilable;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventBus;
import dev.m00nl1ght.clockwork.events.EventDispatcher;
import dev.m00nl1ght.clockwork.events.EventListenerCollection;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EventBusImpl implements EventBus<Event> {

    protected EventBusProfilerGroup profilerGroup;

    protected final Map<Key, EventDispatcher<?, ?>> dispatchers = new HashMap<>();
    protected final Map<Key, EventListenerCollection<?, ?>> listenerCollections = new HashMap<>();

    @Override
    public @NotNull Set<@NotNull EventDispatcher<?, ?>> getEventDispatchers() {
        return Set.copyOf(dispatchers.values());
    }

    @Override
    public <E extends Event, T extends ComponentTarget>
    @NotNull EventDispatcher<E, T> getEventDispatcher(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType) {

        final var key = new Key(eventType, targetType);
        final var existing = dispatchers.get(key);
        if (existing != null) {
            @SuppressWarnings("unchecked")
            final var casted = (EventDispatcher<E, T>) existing;
            return casted;
        } else {
            final var dispatcher = buildDispatcher(eventType, targetType);
            dispatchers.put(key, dispatcher);
            return dispatcher;
        }
    }

    public <E extends Event, T extends ComponentTarget>
    @Nullable EventDispatcher<E, T> getEventDispatcherOrNull(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType) {

        final var key = new Key(eventType, targetType);
        final var existing = dispatchers.get(key);
        if (existing != null) {
            @SuppressWarnings("unchecked")
            final var casted = (EventDispatcher<E, T>) existing;
            return casted;
        } else {
            return null;
        }
    }

    protected <E extends Event, T extends ComponentTarget>
    @NotNull EventDispatcher<E, T> buildDispatcher(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType) {

        final var dispatcher = EventDispatcher.of(eventType, targetType);
        if (profilerGroup != null) profilerGroup.attachToDispatcher(dispatcher);
        for (final var target : dispatcher.getCompatibleTargetTypes()) {
            final var collection = getListenerCollectionOrNull(dispatcher.getEventType(), target);
            if (collection != null) dispatcher.setListenerCollection(collection);
        }
        return dispatcher;
    }

    @Override
    public @NotNull Set<@NotNull EventListenerCollection<?, ?>> getListenerCollections() {
        return Set.copyOf(listenerCollections.values());
    }

    @Override
    public <E extends Event, T extends ComponentTarget>
    @NotNull EventListenerCollection<E, T> getListenerCollection(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType) {

        final var key = new Key(eventType, targetType);
        final var existing = listenerCollections.get(key);
        if (existing != null) {
            @SuppressWarnings("unchecked")
            final var casted = (EventListenerCollection<E, T>) existing;
            return casted;
        } else {
            final var collection = buildListenerCollection(eventType, targetType);
            listenerCollections.put(key, collection);
            return collection;
        }
    }

    protected <E extends Event, T extends ComponentTarget>
    @Nullable EventListenerCollection<E, T> getListenerCollectionOrNull(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType) {

        final var key = new Key(eventType, targetType);
        final var existing = listenerCollections.get(key);
        if (existing != null) {
            @SuppressWarnings("unchecked")
            final var casted = (EventListenerCollection<E, T>) existing;
            return casted;
        } else {
            return null;
        }
    }

    protected <E extends Event, T extends ComponentTarget>
    @NotNull EventListenerCollection<E, T> buildListenerCollection(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType) {

        final var collection = new EventListenerCollectionImpl<>(eventType, targetType);
        for (final var target : collection.getTargetType().getAllParents()) {
            final var dispatcher = getEventDispatcherOrNull(collection.getEventType(), target);
            if (dispatcher != null) dispatcher.setListenerCollection(collection);
        }
        return collection;
    }

    @Override
    public void attachProfiler(@NotNull EventBusProfilerGroup profilerGroup) {
        Objects.requireNonNull(profilerGroup);
        this.profilerGroup = profilerGroup;
        getEventDispatchers().forEach(profilerGroup::attachToDispatcher);
    }

    @Override
    public @NotNull Set<@NotNull ? extends EventBusProfilerGroup> attachDefaultProfilers() {
        final var group = new EventBusProfilerGroup("EventBus", this);
        this.attachProfiler(group);
        return Set.of(group);
    }

    @Override
    public void detachAllProfilers() {
        if (this.profilerGroup == null) return;
        this.profilerGroup = null;
        getEventDispatchers().forEach(Profilable::detachAllProfilers);
    }

    @Override
    public boolean supportsProfilers() {
        return true;
    }

    protected static final class Key {

        public final TypeRef<?> eventType;
        public final TargetType<?> targetType;

        public Key(@NotNull TypeRef<?> eventType, @NotNull TargetType<?> targetType) {
            this.eventType = Objects.requireNonNull(eventType);
            this.targetType = Objects.requireNonNull(targetType);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            final var key = (Key) o;
            return eventType.equals(key.eventType) &&
                    targetType == key.targetType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventType, targetType);
        }

    }

}
