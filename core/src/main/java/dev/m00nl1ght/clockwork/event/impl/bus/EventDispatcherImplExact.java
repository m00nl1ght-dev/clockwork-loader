package dev.m00nl1ght.clockwork.event.impl.bus;

import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.TargetType;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventDispatcher;
import dev.m00nl1ght.clockwork.event.EventListener;
import dev.m00nl1ght.clockwork.event.EventListenerCollection;
import dev.m00nl1ght.clockwork.event.impl.CompiledListeners;
import dev.m00nl1ght.clockwork.utils.profiler.impl.SimpleProfilerGroup;
import dev.m00nl1ght.clockwork.utils.reflect.TypeRef;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class EventDispatcherImplExact<E extends Event, T extends ComponentTarget> implements EventDispatcher<E, T> {

    protected final EventListenerCollection.ChangeObserver observer = this::onListenersChanged;

    protected final TypeRef<E> eventType;
    protected final TargetType<T> targetType;

    protected EventListenerCollection<E, T> listenerCollection;
    protected SimpleProfilerGroup profilerGroup;
    protected CompiledListeners compiledListeners;

    public EventDispatcherImplExact(TypeRef<E> eventType, TargetType<T> targetType) {
        this.eventType = Objects.requireNonNull(eventType);
        this.targetType = Objects.requireNonNull(targetType);
        targetType.requireLocked();
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
        if (listenerCollection != null) listenerCollection.removeObserver(observer, false);
        listenerCollection = (EventListenerCollection<E, T>) collection;
        compiledListeners = null;
        collection.addObserver(observer, false);
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
    public synchronized void attachProfiler(SimpleProfilerGroup profilerGroup) {
        Objects.requireNonNull(profilerGroup);
        this.profilerGroup = profilerGroup;
        this.compiledListeners = null;
    }

    @Override
    public SimpleProfilerGroup attachDefaultProfiler() {
        final var group = new SimpleProfilerGroup(eventType + "@" + targetType);
        this.attachProfiler(group);
        return group;
    }

    @Override
    public synchronized void detachProfiler() {
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
