package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
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
        targetType.getClockworkCore().getState().requireOrAfter(ClockworkCore.State.POPULATED);
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

    public final <C> EventListener<E, T, C> addListener(Class<C> componentClass, BiConsumer<C, E> consumer) {
        return addListener(componentClass, EventListenerPriority.NORMAL, consumer);
    }

    public final <C> EventListener<E, T, C> addListener(Class<C> componentClass, EventListenerPriority priority, BiConsumer<C, E> consumer) {
        if (targetType == null) throw FormatUtil.illStateExc("EventType [] not registered yet", this);
        final var core = targetType.getClockworkCore();
        final var component = core.getComponentType(componentClass, targetType.getTargetClass());
        if (component.isEmpty()) throw FormatUtil.illArgExc("No component type for class [] found", componentClass);
        return this.addListener(component.get(), EventListenerPriority.NORMAL, consumer);
    }

    public final <C> EventListener<E, T, C> addListener(ComponentType<C, T> componentType, BiConsumer<C, E> consumer) {
        return this.addListener(componentType, EventListenerPriority.NORMAL, consumer);
    }

    public final <C> EventListener<E, T, C> addListener(ComponentType<C, T> componentType, EventListenerPriority priority, BiConsumer<C, E> consumer) {
        final var listener = new EventListener<>(eventClassType, componentType, priority, consumer);
        this.addListener(listener);
        return listener;
    }

    public final void addListener(EventListener<E, ? extends T, ?> listener) {
        this.addListeners(List.of(listener));
    }

    public abstract void addListeners(Iterable<EventListener<E, ? extends T, ?>> listeners);

    public final void removeListener(EventListener<E, ? extends T, ?> listener) {
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

    @Override
    public String toString() {
        return targetType == null ? eventClassType + "@?" : eventClassType + "@" + targetType;
    }

    // ### Internal ###

    protected void forTargetAndParents(TargetType<? extends T> origin, Consumer<TargetType<? extends T>> consumer) {
        TargetType<? extends T> type = origin;
        while (type != null) {
            consumer.accept(type);
            @SuppressWarnings("unchecked")
            final var castedType = (TargetType<? extends T>) type.getParent();
            type = castedType;
        }
    }

    protected void checkCompatibility(TargetType<?> otherType) {
        if (targetType == null) {
            final var msg = "Event type for [] is not registered";
            throw new IllegalArgumentException(FormatUtil.format(msg, eventClassType.getType().getTypeName()));
        } else if (!otherType.isEquivalentTo(targetType)) {
            final var msg = "Cannot post event [] (created for target []) to different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, "[]", this, targetType, otherType));
        }
    }

}
