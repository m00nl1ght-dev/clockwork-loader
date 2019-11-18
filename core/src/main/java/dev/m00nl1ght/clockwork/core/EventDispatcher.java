package dev.m00nl1ght.clockwork.core;

import java.util.function.BiConsumer;

public class EventDispatcher<E, T extends ComponentTarget> {

    private Listener<?, E, T> listenerChainFirst;
    private Listener<?, E, T> listenerChainLast;
    protected final ComponentTargetType<T> target;
    protected final Class<E> eventClass;

    public EventDispatcher(ComponentTargetType<T> target, Class<E> eventClass) {
        this.target = target;
        this.eventClass = eventClass;
    }

    public final E post(T object, E event) {
        var c = listenerChainFirst;
        while (c != null) {
            c.accept(event, object);
            c = c.next;
        }
        return event;
    }

    final synchronized <C> void registerListener(ComponentType<C, T> componentType, BiConsumer<C, E> consumer) {
        final var evt = buildListener(componentType, consumer);
        if (listenerChainFirst == null) {
            listenerChainFirst = evt;
            listenerChainLast = evt;
        } else {
            listenerChainLast.next = evt;
            listenerChainLast = evt;
        }
    }

    protected <C> Listener<C, E, T> buildListener(ComponentType<C, T> componentType, BiConsumer<C, E> consumer) {
        return new Listener<>(componentType, consumer);
    }

    public final ComponentTargetType<T> getTarget() {
        return target;
    }

    public Class<E> getEventClass() {
        return eventClass;
    }

    protected static class Listener<C, E, T extends ComponentTarget> {

        protected final ComponentType<C, T> component;
        protected final BiConsumer<C, E> consumer;
        private Listener<?, E, T> next;

        protected Listener(ComponentType<C, T> component, BiConsumer<C, E> consumer) {
            this.component = component;
            this.consumer = consumer;
        }

        protected void accept(E event, T object) {
            final var comp = object.getComponent(component);
            if (comp != null) consumer.accept(comp, event);
        }

    }

}
