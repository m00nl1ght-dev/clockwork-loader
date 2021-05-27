package dev.m00nl1ght.clockwork.event.impl.bus;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.EventBusProfilerGroup;
import dev.m00nl1ght.clockwork.debug.profiler.Profilable;
import dev.m00nl1ght.clockwork.event.*;
import dev.m00nl1ght.clockwork.event.impl.EventDispatchers;
import dev.m00nl1ght.clockwork.event.impl.EventListeners;
import dev.m00nl1ght.clockwork.event.impl.EventTargetKey;
import dev.m00nl1ght.clockwork.util.MapToSet;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public class EventBusImpl implements EventBus<Event> {

    protected final EventDispatchers dispatchers = new EventDispatchers(this::buildDispatcher);
    protected final EventListeners listeners = new EventListeners(this::buildListenerCollection);

    protected final MapToSet<TargetType<?>, EventForwardingPolicy<Event, ?, ?>> forwardingPolicies = new MapToSet<>();

    protected EventBusProfilerGroup profilerGroup;

    @Override
    public <E extends Event, T extends ComponentTarget>
    @NotNull EventDispatcher<E, T> getEventDispatcher(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType) {

        final var key = EventTargetKey.of(eventType, targetType);
        return dispatchers.getDispatcher(key);
    }

    protected <E extends Event, T extends ComponentTarget>
    @NotNull EventDispatcher<E, T> buildDispatcher(@NotNull EventTargetKey<E, T> key) {
        final var dispatcher = EventDispatcher.of(key.getEventType(), key.getTargetType());
        if (profilerGroup != null) profilerGroup.attachToDispatcher(dispatcher);
        for (final var target : dispatcher.getCompatibleTargetTypes()) {
            final var compKey  = EventTargetKey.of(dispatcher.getEventType(), target);
            final var collection = listeners.getCollectionOrNull(compKey);
            if (collection != null) dispatcher.setListenerCollection(collection);
        }
        return dispatcher;
    }

    @Override
    public <E extends Event, T extends ComponentTarget>
    @NotNull EventListenerCollection<E, T> getListenerCollection(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType) {

        final var key = EventTargetKey.of(eventType, targetType);
        return listeners.getCollection(key);
    }

    protected <E extends Event, T extends ComponentTarget>
    @NotNull EventListenerCollection<E, T> buildListenerCollection(@NotNull EventTargetKey<E, T> key) {
        final var collection = new EventListenerCollectionImpl<>(key.getEventType(), key.getTargetType());
        for (final var target : collection.getTargetType().getSelfAndAllParents()) {
            final var compKey  = EventTargetKey.of(collection.getEventType(), target);
            final var dispatcher = dispatchers.getDispatcherOrNull(compKey);
            if (dispatcher != null) dispatcher.setListenerCollection(collection);
        }
        for (final var policy : forwardingPolicies.getValues(key.getTargetType().getRoot())) {
            policy.bind(collection);
        }
        return collection;
    }

    @Override
    public <S extends ComponentTarget, D extends ComponentTarget>
    boolean addForwardingPolicy(@NotNull EventForwardingPolicy<Event, S, D> forwardingPolicy) {
        if (forwardingPolicy.getEventBus() != this) throw new IllegalArgumentException();
        final var rootDest = forwardingPolicy.getDestinationTargetType().getRoot();
        if (!forwardingPolicies.addValue(rootDest, forwardingPolicy)) return false;
        for (final var col : listeners.getCollections()) {
            if (col.getTargetType().getRoot() == rootDest) {
                forwardingPolicy.bind(col);
            }
        }
        return true;
    }

    @Override
    public <S extends ComponentTarget, D extends ComponentTarget>
    boolean removeForwardingPolicy(@NotNull EventForwardingPolicy<Event, S, D> forwardingPolicy) {
        if (forwardingPolicy.getEventBus() != this) throw new IllegalArgumentException();
        final var rootDest = forwardingPolicy.getDestinationTargetType().getRoot();
        if (!forwardingPolicies.removeValue(rootDest, forwardingPolicy)) return false;
        for (final var col : listeners.getCollections()) {
            if (col.getTargetType().getRoot() == rootDest) {
                forwardingPolicy.unbind(col);
            }
        }
        return true;
    }

    @Override
    public @NotNull Set<@NotNull EventForwardingPolicy<Event, ?, ?>> getForwardingPolicies() {
        return forwardingPolicies.getAll();
    }

    @Override
    public void attachProfiler(@NotNull EventBusProfilerGroup profilerGroup) {
        Objects.requireNonNull(profilerGroup);
        this.profilerGroup = profilerGroup;
        dispatchers.getDispatchers().forEach(profilerGroup::attachToDispatcher);
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
        dispatchers.getDispatchers().forEach(Profilable::detachAllProfilers);
    }

    @Override
    public boolean supportsProfilers() {
        return true;
    }

    @Override
    public Class<Event> getBaseEventClass() {
        return Event.class;
    }

}