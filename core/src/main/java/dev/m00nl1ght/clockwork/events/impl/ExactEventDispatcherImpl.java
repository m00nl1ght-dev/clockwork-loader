package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.EventDispatcherProfilerGroup;
import dev.m00nl1ght.clockwork.events.CompiledListeners;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventDispatcher;
import dev.m00nl1ght.clockwork.events.EventListenerCollection;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ExactEventDispatcherImpl<E extends Event, T extends ComponentTarget> implements EventDispatcher<E, T> {

    protected final EventListenerCollection.Observer observer = this::onListenersChanged;

    protected final TypeRef<E> eventType;
    protected final TargetType<T> targetType;

    protected EventListenerCollection<E, T> listenerCollection;
    protected EventDispatcherProfilerGroup<E, T> profilerGroup;
    protected CompiledListeners compiledListeners;

    public ExactEventDispatcherImpl(TypeRef<E> eventType, TargetType<T> targetType) {
        this.eventType = Objects.requireNonNull(eventType);
        this.targetType = Objects.requireNonNull(targetType);
        targetType.requireInitialised();
    }

    protected CompiledListeners compileListeners() {
        final var listeners = CompiledListeners.build(null, listenerCollection, profilerGroup);
        compiledListeners = listeners;
        return listeners;
    }

    protected CompiledListeners getCompiledListeners() {
        if (compiledListeners == null) return compileListeners();
        return compiledListeners;
    }

    private void onListenersChanged(EventListenerCollection<?, ?> collection) {
        compiledListeners = null;
    }

    @Override
    public <S extends T> List<EventListener<E, ? super S, ?>> getListeners(TargetType<S> target) {
        checkCompatibility(target);
        @SuppressWarnings("unchecked")
        final var casted = (EventListener<E, T, ?>[]) getCompiledListeners().listeners;
        return List.of(casted);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends T> EventListenerCollection<E, S> getListenerCollection(TargetType<S> target) {
        checkCompatibility(target);
        return (EventListenerCollection<E, S>) listenerCollection;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends T> void setListenerCollection(EventListenerCollection<E, S> collection) {
        checkCompatibility(collection.getTargetType());
        if (listenerCollection != null) listenerCollection.removeObserver(observer);
        listenerCollection = (EventListenerCollection<E, T>) collection;
        compiledListeners = null;
        collection.addObserver(observer);
    }

    @Override
    public E post(T object, E event) {
        final var container = object.getComponentContainer();
        final TargetType<?> target = container.getTargetType();
        try {
            var listeners = compiledListeners;
            if (listeners == null) listeners = compileListeners();
            event.post(container, listeners);
            return event;
        } catch (Throwable t) {
            checkCompatibility(target);
            throw t;
        }
    }

    @Override
    public final TypeRef<E> getEventType() {
        return eventType;
    }

    @Override
    public final TargetType<T> getTargetType() {
        return targetType;
    }

    @Override
    public Collection<TargetType<? extends T>> getCompatibleTargetTypes() {
        return List.of(targetType);
    }

    protected void checkCompatibility(TargetType<?> otherTarget) {
        if (otherTarget != targetType) {
            final var msg = "Target " + otherTarget + " is not compatible with event dispatcher " + this;
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void attachProfiler(EventDispatcherProfilerGroup<E, ? extends T> profilerGroup) {
        Objects.requireNonNull(profilerGroup);
        if (profilerGroup.getEventType() != this) throw new IllegalArgumentException();
        checkCompatibility(profilerGroup.getTargetType());
        this.profilerGroup = (EventDispatcherProfilerGroup<E, T>) profilerGroup;
        this.compiledListeners = null;
    }

    @Override
    public Set<? extends EventDispatcherProfilerGroup<E, ? extends T>> attachDefaultProfilers() {
        final var group = new EventDispatcherProfilerGroup<>(this, targetType);
        this.attachProfiler(group);
        return Set.of(group);
    }

    @Override
    public synchronized void detachAllProfilers() {
        if (this.profilerGroup == null) return;
        this.profilerGroup = null;
        this.compiledListeners = null;
    }

    @Override
    public boolean supportsProfilers() {
        return true;
    }

    @Override
    public String toString() {
        return eventType + "@" + targetType;
    }

}
