package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.EventDispatcherProfilerGroup;
import dev.m00nl1ght.clockwork.debug.profiler.Profilable;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.EventListenerPriority;
import dev.m00nl1ght.clockwork.events.listener.SimpleEventListener;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

public interface EventDispatcher<E extends Event, T extends ComponentTarget> extends Profilable<EventDispatcherProfilerGroup<E, ? extends T>> {

    E post(T object, E event);

    <S extends T> List<EventListener<E, S, ?>> getListeners(TargetType<S> target);

    List<EventListener<E, ? extends T, ?>> getEffectiveListeners(TargetType<? extends T> target);

    default <C> EventListener<E, T, C> addListener(ComponentType<C, T> componentType, BiConsumer<C, E> consumer) {
        return addListener(componentType, consumer, EventListenerPriority.NORMAL);
    }

    default <C> EventListener<E, T, C> addListener(ComponentType<C, T> componentType, BiConsumer<C, E> consumer, EventListenerPriority priority) {
        final var listener = new SimpleEventListener<>(getEventClassType(), componentType, priority, consumer);
        this.addListener(listener);
        return listener;
    }

    default void addListener(EventListener<E, ? extends T, ?> listener) {
        this.addListeners(List.of(listener));
    }

    void addListeners(Collection<EventListener<E, ? extends T, ?>> listeners);

    default void removeListener(EventListener<E, ? extends T, ?> listener) {
        this.removeListeners(List.of(listener));
    }

    void removeListeners(Collection<EventListener<E, ? extends T, ?>> listeners);

    TypeRef<E> getEventClassType();

    TargetType<T> getTargetType();

    Collection<TargetType<? extends T>> getCompatibleTargetTypes();

}
