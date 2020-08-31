package dev.m00nl1ght.clockwork.benchmarks.event;

import dev.m00nl1ght.clockwork.benchmarks.TestEvent;
import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.EventListener;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.function.BiConsumer;

// for single immutable container, no context aware events

public class EventTypeImpl4<E extends TestEvent, T extends ComponentTarget> extends TestEventType<E, T> {

    private final ComponentContainer<T> container;
    private final T object;

    private BiConsumer[] consumers = new BiConsumer[0];
    private Object[] components = new Object[0];

    @SuppressWarnings("unchecked")
    public EventTypeImpl4(TypeRef<E> eventClassType, T object) {
        super(eventClassType, (TargetType<T>) object.getComponentContainer().getTargetType());
        this.container = (ComponentContainer<T>) object.getComponentContainer();
        this.object = object;
    }

    @SuppressWarnings("unchecked")
    public EventTypeImpl4(Class<E> eventClass, T object) {
        super(eventClass, (TargetType<T>) object.getComponentContainer().getTargetType());
        this.container = (ComponentContainer<T>) object.getComponentContainer();
        this.object = object;
    }

    @Override
    protected void onListenersChanged(TargetType<? extends T> targetType) {
        final var listeners = getEffectiveListeners(targetType);
        listeners.removeIf(l -> container.getComponent(l.getComponentIdx()) == null);
        this.consumers = listeners.stream().map(EventListener::getConsumer).toArray(BiConsumer[]::new);
        this.components = listeners.stream().map(l -> container.getComponent(l.getComponentIdx())).toArray(Object[]::new);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E post(T object, E event) {
        if (object != this.object) checkCompatibility(object.getComponentContainer().getTargetType());
        try {
            for (int i = 0; i < components.length; i++) consumers[i].accept(components[i], event);
            return event;
        } catch (Throwable t) {
            checkCompatibility(object.getComponentContainer().getTargetType());
            throw t;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public E postContextless(T object, E event) {
        if (object != this.object) checkCompatibility(object.getComponentContainer().getTargetType());
        try {
            for (int i = 0; i < components.length; i++) consumers[i].accept(components[i], event);
            return event;
        } catch (Throwable t) {
            checkCompatibility(object.getComponentContainer().getTargetType());
            throw t;
        }
    }

}
