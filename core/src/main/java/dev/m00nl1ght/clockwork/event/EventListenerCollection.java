package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.event.impl.bus.EventListenerCollectionImpl;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Collection;

public interface EventListenerCollection<E extends Event, T extends ComponentTarget> {

    static <E extends Event, T extends ComponentTarget>
    EventListenerCollection<E, T> of(TypeRef<E> eventType, TargetType<T> targetType) {
        return new EventListenerCollectionImpl<>(eventType, targetType);
    }

    static <E extends Event, T extends ComponentTarget>
    EventListenerCollection<E, T> of(Class<E> eventClass, TargetType<T> targetType) {
        return of(TypeRef.of(eventClass), targetType);
    }

    TypeRef<E> getEventType();

    TargetType<T> getTargetType();

    Collection<EventListener<E, T, ?>> get();

    boolean add(EventListener<E, T, ?> listener);

    boolean remove(EventListener<E, T, ?> listener);

    boolean addObserver(Observer observer);

    boolean removeObserver(Observer observer);

    interface Observer {

        void onChange(EventListenerCollection<?, ?> collection);

        default void onAdded(EventListenerCollection<?, ?> collection, EventListener<?, ?, ?> listener) {
            onChange(collection);
        }

        default void onRemoved(EventListenerCollection<?, ?> collection, EventListener<?, ?, ?> listener) {
            onChange(collection);
        }

    }

}
