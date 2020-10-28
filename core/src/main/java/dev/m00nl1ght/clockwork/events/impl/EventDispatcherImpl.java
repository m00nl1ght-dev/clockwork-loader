package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.EventDispatcherProfilerGroup;
import dev.m00nl1ght.clockwork.events.AbstractEventDispatcher;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EventDispatcherImpl<E extends ContextAwareEvent, T extends ComponentTarget> extends AbstractEventDispatcher<E, T> {

    protected final ListenerList[] groupedListeners;
    protected EventDispatcherProfilerGroup[] profilerGroups;

    public EventDispatcherImpl(TypeRef<E> eventType, TargetType<T> targetType) {
        super(eventType, targetType);
        final int cnt = targetType.getSubtargetIdxLast() - idxOffset + 1;
        this.groupedListeners = new ListenerList[cnt];
        Arrays.fill(groupedListeners, ListenerList.empty());
    }

    public EventDispatcherImpl(Class<E> eventClass, TargetType<T> targetType) {
        this(TypeRef.of(eventClass), targetType);
    }

    @Override
    protected <L extends T> void onListenersChanged(TargetType<L> targetType) {
        final List<EventListener<E, ? super L, ?>> listeners = getEffectiveListeners(targetType);
        final int idx = targetType.getSubtargetIdxFirst() - idxOffset;
        @SuppressWarnings("unchecked")
        final var profiler = (EventDispatcherProfilerGroup<E, L>) (profilerGroups == null ? null : profilerGroups[idx]);
        groupedListeners[idx] = listeners.isEmpty() ? ListenerList.empty() : new ListenerList<>(listeners, profiler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E post(T object, E event) {
        final TargetType<?> target = object.getTargetType();
        if (target.getRoot() != rootTarget) checkCompatibility(target);
        try {
            final ListenerList<E, ? super T> group = groupedListeners[target.getSubtargetIdxFirst() - idxOffset];
            if (event.currentContext != null) throw new IllegalStateException();
            event.currentContext = group;
            for (int i = 0; i < group.consumers.length; i++) {
                final Object component = object.getComponent(group.cIdxs[i]);
                try {
                    if (component != null) {
                        event.currentListenerIdx = i;
                        group.consumers[i].accept(component, event);
                    }
                } catch (ExceptionInPlugin e) {
                    e.addComponentToStack(group.listeners.get(i).getComponentType());
                    throw e;
                } catch (Throwable e) {
                    throw ExceptionInPlugin.inEventListener(group.listeners.get(i), event, target, e);
                }
            }
            event.currentContext = null;
            return event;
        } catch (Throwable t) {
            checkCompatibility(target);
            throw t;
        }
    }

    @Override
    public synchronized void attachProfiler(EventDispatcherProfilerGroup<E, ? extends T> profilerGroup) {
        Objects.requireNonNull(profilerGroup);
        if (this.profilerGroups == null) this.profilerGroups = new EventDispatcherProfilerGroup[groupedListeners.length];
        if (profilerGroup.getEventType() != this) throw new IllegalArgumentException();
        checkCompatibility(profilerGroup.getTargetType());
        this.profilerGroups[profilerGroup.getTargetType().getSubtargetIdxFirst() - idxOffset] = profilerGroup;
        onListenersChanged(profilerGroup.getTargetType());
    }

    @Override
    public Set<? extends EventDispatcherProfilerGroup<E, ? extends T>> attachDefaultProfilers() {
        final var groups = targetType.getAllSubtargets().stream()
                .map(t -> new EventDispatcherProfilerGroup<>(this, t))
                .collect(Collectors.toUnmodifiableSet());
        groups.forEach(this::attachProfiler);
        return groups;
    }

    @Override
    public synchronized void detachAllProfilers() {
        if (this.profilerGroups == null) return;
        this.profilerGroups = null;
        for (final var type : targetType.getAllSubtargets()) {
            onListenersChanged(type);
        }
    }

    @Override
    public boolean supportsProfilers() {
        return true;
    }

}
