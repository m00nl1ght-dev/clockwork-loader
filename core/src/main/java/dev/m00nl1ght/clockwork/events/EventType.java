package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class EventType<E extends Event, T extends ComponentTarget> {

    protected final TypeRef<E> eventClassType;
    protected final Class<T> targetClass;

    private TargetType<T> targetType;

    protected EventType(TypeRef<E> eventClassType, Class<T> targetClass) {
        this.eventClassType = eventClassType;
        this.targetClass = targetClass;
    }

    protected EventType(Class<E> eventClass, Class<T> targetClass) {
        this(TypeRef.of(eventClass), targetClass);
    }

    protected EventType(TypeRef<E> eventClassType, TargetType<T> targetType) {
        this(eventClassType, targetType.getTargetClass());
        this.register(targetType);
    }

    protected EventType(Class<E> eventClass, TargetType<T> targetType) {
        this(TypeRef.of(eventClass), targetType);
    }

    public final synchronized void register(TargetType<T> targetType) {
        if (this.targetType != null) throw new IllegalStateException();
        targetType.getPlugin().getClockworkCore().getState().requireOrAfter(ClockworkCore.State.POPULATED);
        this.targetType = targetType;
        init();
    }

    protected abstract void init();

    public abstract E post(T object, E event);

    public abstract <S extends T> List<EventListener<E, S, ?>> getListeners(TargetType<S> target);

    public List<EventListener<E, ? extends T, ?>> getEffectiveListeners(TargetType<? extends T> target) {
        final var list = new ArrayList<EventListener<E, ? extends T, ?>>();
        forTargetAndParents(target, t -> list.addAll(getListeners(t)));
        list.sort(EventListener.PRIORITY_ORDER);
        return list;
    }

    public void addListener(EventListener<E, ? extends T, ?> listener) {
        this.addListeners(List.of(listener));
    }

    public abstract void addListeners(Iterable<EventListener<E, ? extends T, ?>> listeners);

    public void removeListener(EventListener<E, ? extends T, ?> listener) {
        this.removeListeners(List.of(listener));
    }

    public abstract void removeListeners(Iterable<EventListener<E, ? extends T, ?>> listeners);

    public TypeRef<E> getEventClassType() {
        return eventClassType;
    }

    public final Class<T> getTargetClass() {
        return targetClass;
    }

    public final TargetType<T> getTargetType() {
        return targetType;
    }

    protected void forTargetAndParents(TargetType<? extends T> origin, Consumer<TargetType<? extends T>> consumer) {
        TargetType<? extends T> type = origin;
        while (type != null) {
            consumer.accept(type);
            @SuppressWarnings("unchecked")
            final var castedType = (TargetType<? extends T>) type.getParent();
            type = castedType;
        }
    }

}
