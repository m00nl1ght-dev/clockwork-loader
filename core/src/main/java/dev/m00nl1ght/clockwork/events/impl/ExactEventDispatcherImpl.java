package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.EventDispatcherProfilerGroup;
import dev.m00nl1ght.clockwork.events.AbstractExactEventDispatcher;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Set;

public class ExactEventDispatcherImpl<E extends ContextAwareEvent, T extends ComponentTarget> extends AbstractExactEventDispatcher<E, T> {

    protected ListenerList groupedListeners = ListenerList.EMPTY;
    protected EventDispatcherProfilerGroup<E, T> profilerGroup;

    public ExactEventDispatcherImpl(TypeRef<E> eventClassType, TargetType<T> targetType) {
        super(eventClassType, targetType);
    }

    public ExactEventDispatcherImpl(Class<E> eventClass, TargetType<T> targetType) {
        this(TypeRef.of(eventClass), targetType);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onListenersChanged() {
        groupedListeners = listeners.isEmpty() ? ListenerList.EMPTY : new ListenerList(listeners, profilerGroup);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E post(T object, E event) {
        final TargetType<?> target = object.getTargetType();
        if (target != targetType) checkCompatibility(target);
        try {
            if (event.currentContext != null) throw new IllegalStateException();
            event.currentContext = groupedListeners;
            for (int i = 0; i < groupedListeners.consumers.length; i++) {
                final Object component = object.getComponent(groupedListeners.cIdxs[i]);
                try {
                    if (component != null) {
                        event.currentListenerIdx = i;
                        groupedListeners.consumers[i].accept(component, event);
                    }
                } catch (ExceptionInPlugin e) {
                    e.addComponentToStack(groupedListeners.listeners.get(i).getComponentType());
                    throw e;
                } catch (Throwable e) {
                    throw ExceptionInPlugin.inEventListener(groupedListeners.listeners.get(i), event, target, e);
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
    @SuppressWarnings("unchecked")
    public synchronized void attachProfiler(EventDispatcherProfilerGroup<E, ? extends T> profilerGroup) {
        Arguments.notNull(profilerGroup, "profilerGroup");
        if (profilerGroup.getEventType() != this) throw new IllegalArgumentException();
        checkCompatibility(profilerGroup.getTargetType());
        this.profilerGroup = (EventDispatcherProfilerGroup<E, T>) profilerGroup;
        onListenersChanged();
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
        onListenersChanged();
    }

    @Override
    public boolean supportsProfilers() {
        return true;
    }

}
