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
import java.util.Objects;
import java.util.function.BiConsumer;

public interface EventDispatcher<E extends Event, T extends ComponentTarget> extends Profilable<EventDispatcherProfilerGroup<E, ? extends T>> {

    E post(T object, E event);

    <S extends T> List<EventListener<E, S, ?>> getListeners(TargetType<S> target);

    <S extends T> List<EventListener<E, ? super S, ?>> getEffectiveListeners(TargetType<S> target);

    default <S extends T, C> EventListener<E, S, C> addListener(ComponentType<C, S> componentType, BiConsumer<C, E> consumer) {
        return addListener(componentType, consumer, EventListenerPriority.NORMAL);
    }

    default <S extends T, C> EventListener<E, S, C> addListener(ComponentType<C, S> componentType, BiConsumer<C, E> consumer, EventListenerPriority priority) {
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

    class Key {

        public final TypeRef<?> eventType;
        public final Class<?> targetClass;

        public Key(TypeRef<?> eventType, Class<?> targetClass) {
            this.eventType = Objects.requireNonNull(eventType);
            this.targetClass = Objects.requireNonNull(targetClass);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return eventType.equals(key.eventType) &&
                    targetClass == key.targetClass;
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventType, targetClass);
        }

    }

}
