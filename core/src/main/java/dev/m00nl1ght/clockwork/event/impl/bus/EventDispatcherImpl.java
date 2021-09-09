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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class EventDispatcherImpl<E extends Event, T extends ComponentTarget> implements EventDispatcher<E, T> {

    protected final EventListenerCollection.ChangeObserver<E> observer = this::onListenersChanged;

    protected final TypeRef<E> eventType;
    protected final TargetType<T> targetType;
    protected final int idxOffset;

    protected final EventListenerCollection[] listenerCollections;
    protected final CompiledListeners[] compiledListeners;
    protected SimpleProfilerGroup[] profilerGroups;

    public EventDispatcherImpl(TypeRef<E> eventType, TargetType<T> targetType) {
        this.eventType = Objects.requireNonNull(eventType);
        this.targetType = Objects.requireNonNull(targetType);
        targetType.requireLocked();
        this.idxOffset = targetType.getSubtargetIdxFirst();
        final int cnt = targetType.getSubtargetIdxLast() - idxOffset + 1;
        this.listenerCollections = new EventListenerCollection[cnt];
        this.compiledListeners = new CompiledListeners[cnt];
    }

    protected CompiledListeners compileListeners(TargetType<?> targetType) {
        final int idx = targetType.getSubtargetIdxFirst() - idxOffset;
        final var profiler = profilerGroups == null ? null : profilerGroups[idx];
        final var inherited = targetType == this.targetType ? null : getCompiledListeners(targetType.getParent());
        final var listeners = CompiledListeners.build(inherited, listenerCollections[idx], profiler);
        compiledListeners[idx] = listeners;
        return listeners;
    }

    protected CompiledListeners getCompiledListeners(TargetType<?> targetType) {
        final var existing = compiledListeners[targetType.getSubtargetIdxFirst() - idxOffset];
        if (existing == null) return compileListeners(targetType);
        return existing;
    }

    private void onListenersChanged(EventListenerCollection<?, ?> collection) {
        compiledListeners[collection.getTargetType().getSubtargetIdxFirst() - idxOffset] = null;
    }

    @Override
    public <S extends T> List<EventListener<E, ? super S, ?>> getListeners(TargetType<S> target) {
        checkCompatibility(target);
        @SuppressWarnings("unchecked")
        final var casted = (EventListener<E, ? super S, ?>[]) getCompiledListeners(target).listeners;
        return List.of(casted);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends T> EventListenerCollection<E, S> getListenerCollection(TargetType<S> target) {
        checkCompatibility(target);
        return listenerCollections[target.getSubtargetIdxFirst() - idxOffset];
    }

    @Override
    public <S extends T> void setListenerCollection(EventListenerCollection<E, S> collection) {
        checkCompatibility(collection.getTargetType());
        final var idx = collection.getTargetType().getSubtargetIdxFirst() - idxOffset;
        @SuppressWarnings("unchecked")
        final var old = (EventListenerCollection<E, S>) listenerCollections[idx];
        if (old != null) old.removeObserver(observer, false);
        listenerCollections[idx] = collection;
        compiledListeners[idx] = null;
        collection.addObserver(observer, false);
    }

    @Override
    public E post(T object, E event) {
        final var container = object.getComponentContainer();
        final TargetType<?> target = container.getTargetType();
        try {
            var listeners = compiledListeners[target.getSubtargetIdxFirst() - idxOffset];
            if (listeners == null) listeners = compileListeners(target);
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
        return targetType.getAllSubtargets();
    }

    protected void checkCompatibility(TargetType<?> otherTarget) {
        if (!otherTarget.isEquivalentTo(targetType)) {
            final var msg = "Target " + otherTarget + " is not compatible with event dispatcher " + this;
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public synchronized void attachProfiler(SimpleProfilerGroup profilerGroup) {
        Objects.requireNonNull(profilerGroup);
        this.profilerGroups = new SimpleProfilerGroup[compiledListeners.length];
        for (final var target : targetType.getAllSubtargets()) {
            final var idx = target.getSubtargetIdxFirst() - idxOffset;
            this.profilerGroups[idx] = profilerGroup.subgroup(target.toString(), SimpleProfilerGroup::new);
            this.compiledListeners[idx] = null;
        }
    }

    @Override
    public SimpleProfilerGroup attachDefaultProfiler() {
        final var group = new SimpleProfilerGroup(eventType + "@" + targetType);
        this.attachProfiler(group);
        return group;
    }

    @Override
    public synchronized void detachProfiler() {
        if (this.profilerGroups == null) return;
        this.profilerGroups = null;
        Arrays.fill(compiledListeners, null);
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
