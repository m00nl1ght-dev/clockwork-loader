package dev.m00nl1ght.clockwork.event.impl.bus;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventListenerCollection;
import dev.m00nl1ght.clockwork.event.EventListener;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.*;

public class EventListenerCollectionImpl<E extends Event, T extends ComponentTarget> implements EventListenerCollection<E, T> {

    protected final TypeRef<E> eventType;
    protected final TargetType<T> targetType;

    protected Set<EventListener<E, T, ?>> set;
    protected List<Observer<? super E>> observers;

    public EventListenerCollectionImpl(TypeRef<E> eventType, TargetType<T> targetType) {
        this.eventType = Objects.requireNonNull(eventType);
        this.targetType = Objects.requireNonNull(targetType);
    }

    @Override
    public final TypeRef<E> getEventType() {
        return eventType;
    }

    @Override
    public final TargetType<T> getTargetType() {
        return targetType;
    }

    @Override
    public Collection<EventListener<E, T, ?>> get() {
        return set == null ? Collections.emptySet() : Collections.unmodifiableSet(set);
    }

    @Override
    public boolean add(EventListener<E, T, ?> listener) {
        Objects.requireNonNull(listener);
        if (set == null) set = new HashSet<>(5);
        final var changed = set.add(listener);
        if (changed && observers != null)
            for (final var observer : observers)
                observer.onAdded(this, listener);
        return changed;
    }

    @Override
    public boolean remove(EventListener<E, T, ?> listener) {
        Objects.requireNonNull(listener);
        if (set == null) return false;
        final var changed = set.remove(listener);
        if (changed && observers != null)
            for (final var observer : observers)
                observer.onRemoved(this, listener);
        return changed;
    }

    @Override
    public boolean addObserver(Observer<? super E> observer) {
        Objects.requireNonNull(observer);
        if (observers == null) observers = new LinkedList<>();
        if (observers.contains(observer)) return false;
        observers.add(observer);
        return true;
    }

    @Override
    public boolean removeObserver(Observer<? super E> observer) {
        Objects.requireNonNull(observer);
        if (observers == null) return false;
        return observers.remove(observer);
    }

}
