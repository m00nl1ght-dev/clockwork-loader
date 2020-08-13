package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;

import java.util.List;

public abstract class EventType<E extends Event, T extends ComponentTarget> {

    protected final Class<E> eventClass;
    protected final Class<T> targetClass;

    private TargetType<T> targetType;

    protected EventType(Class<E> eventClass, Class<T> targetClass) {
        this.eventClass = eventClass;
        this.targetClass = targetClass;
    }

    public final synchronized void register(TargetType<T> targetType) {
        if (this.targetType == targetType) return;
        if (this.targetType != null) throw new IllegalStateException();
        targetType.registerEventType(this);
        this.targetType = targetType;
        init();
    }

    protected abstract void init();

    public abstract E post(T object, E event);

    public abstract <S extends T> List<EventListener<E, ? super S, ?>> getListeners(TargetType<S> target);

    public abstract <C> void addListener(EventListener<E, T, C> listener);

    public abstract <C> void removeListener(EventListener<E, T, C> listener);

    public final Class<E> getEventClass() {
        return eventClass;
    }

    public final Class<T> getTargetClass() {
        return targetClass;
    }

    public final TargetType<T> getTargetType() {
        return targetType;
    }

}
