package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.EventProfilerGroup;
import dev.m00nl1ght.clockwork.events.ListenerList;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.TypeRef;

public class EventTypeImplExact<E extends ContextAwareEvent, T extends ComponentTarget> extends BasicEventTypeExact<E, T> {

    protected ListenerList groupedListeners = ListenerList.EMPTY;
    protected EventProfilerGroup<E, T> profilerGroup;

    public EventTypeImplExact(TypeRef<E> eventClassType, TargetType<T> targetType) {
        super(eventClassType, targetType);
    }

    public EventTypeImplExact(Class<E> eventClass, TargetType<T> targetType) {
        this(TypeRef.of(eventClass), targetType);
    }

    @Override
    protected void onListenersChanged() {
        groupedListeners = listeners.isEmpty() ? ListenerList.EMPTY : new ListenerList(getListeners(), profilerGroup);
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
    public synchronized void attachProfiler(EventProfilerGroup<E, ? extends T> profilerGroup) {
        Arguments.notNull(profilerGroup, "profilerGroup");
        if (profilerGroup.getEventType() != this) throw new IllegalArgumentException();
        checkCompatibility(profilerGroup.getTargetType());
        this.profilerGroup = (EventProfilerGroup<E, T>) profilerGroup;
        onListenersChanged();
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
