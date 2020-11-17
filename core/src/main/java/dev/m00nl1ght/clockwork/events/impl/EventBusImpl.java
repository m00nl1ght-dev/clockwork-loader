package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.debug.profiler.EventBusProfilerGroup;
import dev.m00nl1ght.clockwork.debug.profiler.Profilable;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventBus;
import dev.m00nl1ght.clockwork.events.EventDispatcher;
import dev.m00nl1ght.clockwork.events.EventListenerCollection;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.EventListenerPriority;
import dev.m00nl1ght.clockwork.events.listener.SimpleEventListener;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

public class EventBusImpl implements EventBus<Event> {

    protected final ClockworkCore core;
    protected EventBusProfilerGroup profilerGroup;

    protected final Map<Key, EventDispatcher<?, ?>> dispatchers = new HashMap<>();
    protected final Map<Key, EventListenerCollection<?, ?>> listenerCollections = new HashMap<>();

    public EventBusImpl(@NotNull ClockworkCore core) {
        this.core = Objects.requireNonNull(core);
        core.getState().requireOrAfter(ClockworkCore.State.PROCESSED);
    }

    @Override
    @NotNull
    public Set<@NotNull EventDispatcher<?, ?>> getEventDispatchers() {
        return Set.copyOf(dispatchers.values());
    }

    @Override
    @NotNull
    public <E extends Event, T extends ComponentTarget>
    EventDispatcher<E, T> getEventDispatcher(
            @NotNull TypeRef<E> eventType,
            @NotNull Class<T> targetClass) {

        final var key = new Key(eventType, targetClass);
        final var existing = dispatchers.get(key);
        if (existing != null) {
            @SuppressWarnings("unchecked")
            final var casted = (EventDispatcher<E, T>) existing;
            return casted;
        } else {
            final var dispatcher = buildDispatcher(eventType, targetClass);
            dispatchers.put(key, dispatcher);
            return dispatcher;
        }
    }

    @Nullable
    public <E extends Event, T extends ComponentTarget>
    EventDispatcher<E, T> getEventDispatcherOrNull(
            @NotNull TypeRef<E> eventType,
            @NotNull Class<T> targetClass) {

        final var key = new Key(eventType, targetClass);
        final var existing = dispatchers.get(key);
        if (existing != null) {
            @SuppressWarnings("unchecked")
            final var casted = (EventDispatcher<E, T>) existing;
            return casted;
        } else {
            return null;
        }
    }

    @NotNull
    public <E extends Event>
    EventDispatcher<E, ClockworkCore> getEventDispatcher(@NotNull TypeRef<E> eventType) {
        return getEventDispatcher(eventType, ClockworkCore.class);
    }

    @NotNull
    public <E extends Event>
    EventDispatcher<E, ClockworkCore> getEventDispatcher(@NotNull Class<E> eventClass) {
        return getEventDispatcher(TypeRef.of(eventClass));
    }

    @NotNull
    protected <E extends Event, T extends ComponentTarget>
    EventDispatcher<E, T> buildDispatcher(
            @NotNull TypeRef<E> eventType,
            @NotNull Class<T> targetClass) {

        final var target = core.getTargetTypeOrThrow(targetClass);
        if (target.getDirectSubtargets().isEmpty()) {
            return linkDispatcher(new ExactEventDispatcherImpl<>(eventType, target));
        } else {
            return linkDispatcher(new EventDispatcherImpl<>(eventType, target));
        }
    }

    @Override
    @NotNull
    public Set<@NotNull EventListenerCollection<?, ?>> getListenerCollections() {
        return Set.copyOf(listenerCollections.values());
    }

    @Override
    @NotNull
    public <E extends Event, T extends ComponentTarget>
    EventListenerCollection<E, T> getListenerCollection(
            @NotNull TypeRef<E> eventType,
            @NotNull Class<T> targetClass) {

        final var key = new Key(eventType, targetClass);
        final var existing = listenerCollections.get(key);
        if (existing != null) {
            @SuppressWarnings("unchecked")
            final var casted = (EventListenerCollection<E, T>) existing;
            return casted;
        } else {
            final var collection = buildListenerCollection(eventType, targetClass);
            listenerCollections.put(key, collection);
            return collection;
        }
    }

    @Nullable
    protected <E extends Event, T extends ComponentTarget>
    EventListenerCollection<E, T> getListenerCollectionOrNull(
            @NotNull TypeRef<E> eventType,
            @NotNull Class<T> targetClass) {

        final var key = new Key(eventType, targetClass);
        final var existing = listenerCollections.get(key);
        if (existing != null) {
            @SuppressWarnings("unchecked")
            final var casted = (EventListenerCollection<E, T>) existing;
            return casted;
        } else {
            return null;
        }
    }

    @NotNull
    public <E extends Event>
    EventListenerCollection<E, ClockworkCore> getListenerCollection(@NotNull TypeRef<E> eventType) {
        return getListenerCollection(eventType, ClockworkCore.class);
    }

    @NotNull
    public <E extends Event>
    EventListenerCollection<E, ClockworkCore> getListenerCollection(@NotNull Class<E> eventClass) {
        return getListenerCollection(TypeRef.of(eventClass));
    }

    @NotNull
    protected <E extends Event, T extends ComponentTarget>
    EventListenerCollection<E, T> buildListenerCollection(
            @NotNull TypeRef<E> eventType,
            @NotNull Class<T> targetClass) {

        final var target = core.getTargetTypeOrThrow(targetClass);
        return linkListenerCollection(new EventListenerCollectionImpl<>(eventType, target));
    }

    @NotNull
    protected <E extends Event, T extends ComponentTarget>
    EventDispatcher<E, T> linkDispatcher(@NotNull EventDispatcher<E, T> dispatcher) {
        if (profilerGroup != null) profilerGroup.attachToDispatcher(dispatcher);
        for (final var target : dispatcher.getCompatibleTargetTypes()) {
            final var collection = getListenerCollectionOrNull(dispatcher.getEventType(), target.getTargetClass());
            if (collection != null) dispatcher.setListenerCollection(collection);
        }
        return dispatcher;
    }

    @NotNull
    protected <E extends Event, T extends ComponentTarget>
    EventListenerCollection<E, T> linkListenerCollection(@NotNull EventListenerCollection<E, T> collection) {
        for (final var target : collection.getTargetType().getAllParents()) {
            final var dispatcher = getEventDispatcherOrNull(collection.getEventType(), target.getTargetClass());
            if (dispatcher != null) dispatcher.setListenerCollection(collection);
        }
        return collection;
    }

    @NotNull
    public <E extends Event, T extends ComponentTarget, C>
    EventListener<E, T, C> addListener(
            @NotNull TypeRef<E> eventType,
            @NotNull ComponentType<C, T> componentType,
            @NotNull BiConsumer<C, E> consumer,
            @NotNull EventListenerPriority priority) {

        final var listener = new SimpleEventListener<>(eventType, componentType, priority, consumer);
        getListenerCollection(eventType, componentType.getTargetType().getTargetClass()).add(listener);
        return listener;
    }

    @Override
    @NotNull
    public <E extends Event, T extends ComponentTarget, C>
    EventListener<E, T, C> addListener(
            @NotNull TypeRef<E> eventType,
            @NotNull Class<T> targetClass,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer,
            @NotNull EventListenerPriority priority) {

        final var component = core.getComponentTypeOrThrow(componentClass, targetClass);
        return addListener(eventType, component, consumer, priority);
    }

    @NotNull
    public <E extends Event, C>
    EventListener<E, ?, C> addListener( // TODO re-evaluate
            @NotNull TypeRef<E> eventType,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer,
            @NotNull EventListenerPriority priority) {

        final var component = core.getComponentTypeOrThrow(componentClass);
        return addListener(eventType, component, consumer, priority);
    }

    @NotNull
    public <E extends Event, C>
    EventListener<E, ?, C> addListener(
            @NotNull Class<E> eventClass,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer,
            @NotNull EventListenerPriority priority) {

        return addListener(TypeRef.of(eventClass), componentClass, consumer, priority);
    }

    @NotNull
    public <E extends Event, C>
    EventListener<E, ?, C> addListener(
            @NotNull TypeRef<E> eventType,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer) {

        return addListener(eventType, componentClass, consumer, EventListenerPriority.NORMAL);
    }

    @NotNull
    public <E extends Event, C>
    EventListener<E, ?, C> addListener(
            @NotNull Class<E> eventClass,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer) {

        return addListener(eventClass, componentClass, consumer, EventListenerPriority.NORMAL);
    }

    @Override
    public void attachProfiler(@NotNull EventBusProfilerGroup profilerGroup) {
        Objects.requireNonNull(profilerGroup);
        this.profilerGroup = profilerGroup;
        getEventDispatchers().forEach(profilerGroup::attachToDispatcher);
    }

    @Override
    @NotNull
    public Set<@NotNull ? extends EventBusProfilerGroup> attachDefaultProfilers() {
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

    @NotNull
    public final ClockworkCore getCore() {
        return core;
    }

    protected static final class Key {

        public final TypeRef<?> eventType;
        public final Class<?> targetClass;

        public Key(@NotNull TypeRef<?> eventType, @NotNull Class<?> targetClass) {
            this.eventType = Objects.requireNonNull(eventType);
            this.targetClass = Objects.requireNonNull(targetClass);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            final var key = (Key) o;
            return eventType.equals(key.eventType) &&
                    targetClass == key.targetClass;
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventType, targetClass);
        }

    }

}
