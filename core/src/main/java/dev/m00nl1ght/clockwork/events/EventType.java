package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.EventProfilerGroup;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.SimpleEventListener;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class EventType<E extends Event, T extends ComponentTarget> {

    protected final TypeRef<E> eventClassType;
    protected final TargetType<T> targetType;

    protected EventType(TypeRef<E> eventClassType, TargetType<T> targetType) {
        this.eventClassType = Arguments.notNull(eventClassType, "eventClassType");
        this.targetType = Arguments.notNull(targetType, "targetType");
        targetType.requireInitialised();
    }

    public abstract E post(T object, E event);

    public abstract <S extends T> List<EventListener<E, S, ?>> getListeners(TargetType<S> target);

    public List<EventListener<E, ? extends T, ?>> getEffectiveListeners(TargetType<? extends T> target) {
        final var list = new ArrayList<EventListener<E, ? extends T, ?>>();
        forTargetAndParents(target, t -> list.addAll(getListeners(t)));
        list.sort(EventListener.PRIORITY_ORDER);
        return list;
    }

    public final <C> EventListener<E, T, C> addListener(ClockworkCore core, Class<C> componentClass, BiConsumer<C, E> consumer) {
        return addListener(core, componentClass, EventListenerPriority.NORMAL, consumer);
    }

    public final <C> EventListener<E, T, C> addListener(ClockworkCore core, Class<C> componentClass, EventListenerPriority priority, BiConsumer<C, E> consumer) {
        if (targetType == null) throw FormatUtil.illStateExc("EventType [] not registered yet", this);
        final var component = core.getComponentType(componentClass, targetType.getTargetClass());
        if (component.isEmpty()) throw FormatUtil.illArgExc("No component type for class [] found", componentClass);
        return this.addListener(component.get(), EventListenerPriority.NORMAL, consumer);
    }

    public final <C> EventListener<E, T, C> addListener(ComponentType<C, T> componentType, BiConsumer<C, E> consumer) {
        return this.addListener(componentType, EventListenerPriority.NORMAL, consumer);
    }

    public final <C> EventListener<E, T, C> addListener(ComponentType<C, T> componentType, EventListenerPriority priority, BiConsumer<C, E> consumer) {
        final var listener = new SimpleEventListener<>(eventClassType, componentType, priority, consumer);
        this.addListener(listener);
        return listener;
    }

    public final void addListener(EventListener<E, ? extends T, ?> listener) {
        this.addListeners(List.of(listener));
    }

    public abstract void addListeners(Collection<EventListener<E, ? extends T, ?>> listeners);

    public final void removeListener(EventListener<E, ? extends T, ?> listener) {
        this.removeListeners(List.of(listener));
    }

    public abstract void removeListeners(Collection<EventListener<E, ? extends T, ?>> listeners);

    public TypeRef<E> getEventClassType() {
        return eventClassType;
    }

    public final TargetType<T> getTargetType() {
        return targetType;
    }

    public Collection<TargetType<? extends T>> getCompatibleTargetTypes() {
        return targetType.getAllSubtargets();
    }

    public void attachProfiler(EventProfilerGroup<E, ? extends T> profilerGroup) {
        throw FormatUtil.unspExc("This EventType implementation does not support profilers");
    }

    public void detachAllProfilers() {}

    public boolean supportsProfilers() {
        return false;
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
            if (type == this.targetType) break;
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
            final var msg = "Cannot post event [] to different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, this, otherType));
        }
    }

}
