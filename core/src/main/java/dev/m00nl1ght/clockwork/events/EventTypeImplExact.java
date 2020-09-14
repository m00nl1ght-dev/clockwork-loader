package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.util.TypeRef;

public class EventTypeImplExact<E extends Event, T extends ComponentTarget> extends BasicEventTypeExact<E, T> {

    private ListenerList groupedListeners = ListenerList.EMPTY;
    private TargetType<T> exactType;

    public EventTypeImplExact(TypeRef<E> eventClassType, Class<T> targetClass) {
        super(eventClassType, targetClass);
    }

    public EventTypeImplExact(Class<E> eventClass, Class<T> targetClass) {
        super(eventClass, targetClass);
    }

    public EventTypeImplExact(TypeRef<E> eventClassType, TargetType<T> targetType) {
        super(eventClassType, targetType);
    }

    public EventTypeImplExact(Class<E> eventClass, TargetType<T> targetType) {
        super(eventClass, targetType);
    }

    @Override
    protected void init() {
        this.exactType = getTargetType();
    }

    @Override
    protected void onListenersChanged() {
        groupedListeners = listeners.isEmpty() ? ListenerList.EMPTY : new ListenerList(getListeners());
    }

    @Override
    @SuppressWarnings("unchecked")
    public E post(T object, E event) {
        final TargetType<?> target = object.getTargetType();
        if (target != exactType) checkCompatibility(target);
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

}
