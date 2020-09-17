package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.EventProfilerGroup;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Arrays;
import java.util.List;

public class EventTypeImpl<E extends Event, T extends ComponentTarget> extends BasicEventType<E, T> {

    protected ListenerList[] groupedListeners;
    protected EventProfilerGroup[] profilerGroups;

    public EventTypeImpl(TypeRef<E> eventClassType, Class<T> targetClass) {
        super(eventClassType, targetClass);
    }

    public EventTypeImpl(Class<E> eventClass, Class<T> targetClass) {
        super(eventClass, targetClass);
    }

    public EventTypeImpl(TypeRef<E> eventClassType, TargetType<T> targetType) {
        super(eventClassType, targetType);
    }

    public EventTypeImpl(Class<E> eventClass, TargetType<T> targetType) {
        super(eventClass, targetType);
    }

    @Override
    protected void init() {
        super.init();
        final int cnt = getTargetType().getSubtargetIdxLast() - idxOffset + 1;
        this.groupedListeners = new ListenerList[cnt];
        Arrays.fill(groupedListeners, ListenerList.EMPTY);
    }

    @Override
    protected void onListenersChanged(TargetType<? extends T> targetType) {
        final List<EventListener<E, ? extends T, ?>> listeners = getEffectiveListeners(targetType);
        final int idx = targetType.getSubtargetIdxFirst() - idxOffset;
        final var profiler = profilerGroups == null ? null : profilerGroups[idx];
        groupedListeners[idx] = listeners.isEmpty() ? ListenerList.EMPTY : new ListenerList(listeners, profiler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E post(T object, E event) {
        final TargetType<?> target = object.getTargetType();
        if (target.getRoot() != rootTarget) checkCompatibility(target);
        try {
            final ListenerList group = groupedListeners[target.getSubtargetIdxFirst() - idxOffset];
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
    public synchronized void attachProfiler(EventProfilerGroup<E, ? extends T> profilerGroup) {
        Arguments.notNull(profilerGroup, "profilerGroup");
        if (this.profilerGroups == null) this.profilerGroups = new EventProfilerGroup[groupedListeners.length];
        if (profilerGroup.getEventType() != this) throw new IllegalArgumentException();
        checkCompatibility(profilerGroup.getTargetType());
        this.profilerGroups[profilerGroup.getTargetType().getSubtargetIdxFirst() - idxOffset] = profilerGroup;
        onListenersChanged(profilerGroup.getTargetType());
    }

    @Override
    public synchronized void detachAllProfilers() {
        if (this.profilerGroups == null) return;
        this.profilerGroups = null;
        for (final var type : getTargetType().getAllSubtargets()) {
            onListenersChanged(type);
        }
    }

    @Override
    public boolean supportsProfilers() {
        return true;
    }

}
