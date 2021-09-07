package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.TargetType;
import dev.m00nl1ght.clockwork.event.impl.bus.EventListenerCollectionImpl;
import dev.m00nl1ght.clockwork.utils.reflect.TypeRef;

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

    boolean addObserver(Observer<? super E> observer, boolean notifyNow);

    boolean removeObserver(Observer<? super E> observer, boolean notifyNow);

    interface Observer<B extends Event> {

        <E extends B, T extends ComponentTarget, C>
        void onAdded(EventListenerCollection<E, T> collection, EventListener<E, T, C> listener);

        <E extends B, T extends ComponentTarget, C>
        void onRemoved(EventListenerCollection<E, T> collection, EventListener<E, T, C> listener);

    }

    interface ChangeObserver<B extends Event> extends Observer<B> {

        <E extends B, T extends ComponentTarget>
        void onChange(EventListenerCollection<E, T> collection);

        @Override
        default <E extends B, T extends ComponentTarget, C>
        void onAdded(EventListenerCollection<E, T> collection, EventListener<E, T, C> listener) {
            onChange(collection);
        }

        @Override
        default <E extends B, T extends ComponentTarget, C>
        void onRemoved(EventListenerCollection<E, T> collection, EventListener<E, T, C> listener) {
            onChange(collection);
        }

    }

}
