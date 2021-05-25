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
    protected Observer observer;

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
        if (changed && observer != null) {
            observer.onAdded(this, listener);
        }
        return changed;
    }

    @Override
    public boolean remove(EventListener<E, T, ?> listener) {
        Objects.requireNonNull(listener);
        if (set == null) return false;
        final var changed = set.remove(listener);
        if (changed && observer != null) {
            observer.onRemoved(this, listener);
        }
        return changed;
    }

    @Override
    public boolean addObserver(Observer observer) {
        Objects.requireNonNull(observer);
        if (this.observer == null) {
            this.observer = observer;
            return true;
        } else if (this.observer instanceof CombinedObserver) {
            return ((CombinedObserver) this.observer).getObservers().add(observer);
        } else if (this.observer.equals(observer)) {
            return false;
        } else {
            this.observer = new CombinedObserver(Set.of(this.observer, observer));
            return true;
        }
    }

    @Override
    public boolean removeObserver(Observer observer) {
        Objects.requireNonNull(observer);
        if (this.observer.equals(observer)) {
            this.observer = null;
            return true;
        } else if (this.observer instanceof CombinedObserver) {
            return ((CombinedObserver) this.observer).getObservers().remove(observer);
        } else {
            return false;
        }
    }

    protected static class CombinedObserver implements Observer {

        private final Set<Observer> observers = new HashSet<>(3);

        public CombinedObserver(Collection<Observer> observers) {
            this.observers.addAll(observers);
        }

        @Override
        public void onChange(EventListenerCollection<?, ?> collection) {
            observers.forEach(o -> o.onChange(collection));
        }

        @Override
        public void onAdded(EventListenerCollection<?, ?> collection, EventListener<?, ?, ?> listener) {
            observers.forEach(o -> o.onAdded(collection, listener));
        }

        @Override
        public void onRemoved(EventListenerCollection<?, ?> collection, EventListener<?, ?, ?> listener) {
            observers.forEach(o -> o.onRemoved(collection, listener));
        }

        public Set<Observer> getObservers() {
            return observers;
        }

    }

}
