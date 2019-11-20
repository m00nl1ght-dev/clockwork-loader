package dev.m00nl1ght.clockwork.core;

import java.util.function.BiConsumer;

public class EventDispatcher<E, T extends ComponentTarget<? super T>> {

    private Listener<?, E, T> listenerChainFirst;
    private Listener<?, E, T> listenerChainLast;
    protected final TargetType<T> target;
    protected final Class<E> eventClass;

    public EventDispatcher(TargetType<T> target, Class<E> eventClass) {
        this.target = target;
        this.eventClass = eventClass;
    }

    public E post(T object, E event) {
        Listener<?, E, ? super T> c = listenerChainFirst;
        while (c != null) {
            c.accept(event, object);
            c = c.next;
        }
        return event;
    }

    protected synchronized <C> void registerListener(ComponentType<C, T> componentType, BiConsumer<C, E> consumer, EventFilter<E, T> filter) {
        final var evt = buildListener(componentType, consumer, filter);
        if (listenerChainFirst == null) {
            listenerChainFirst = evt;
            listenerChainLast = evt;
        } else {
            listenerChainLast.next = evt;
            listenerChainLast = evt;
        }
    }

    protected <C> Listener<C, E, T> buildListener(ComponentType<C, T> componentType, BiConsumer<C, E> consumer, EventFilter<E, T> filter) {
        if (filter == null) {
            return new SimpleListener<>(componentType, consumer);
        } else {
            return new FilteredListener<>(componentType, consumer, filter);
        }
    }

    protected synchronized void linkToParent(EventDispatcher<E, ? super T> parent) {
        listenerChainLast.next = parent.listenerChainFirst;
    }

    public final TargetType<T> getTarget() {
        return target;
    }

    public final Class<E> getEventClass() {
        return eventClass;
    }

    protected static abstract class Listener<C, E, T extends ComponentTarget<? super T>> {

        protected final ComponentType<C, T> component;
        protected final BiConsumer<C, E> consumer;
        private Listener<?, E, ? super T> next;

        protected Listener(ComponentType<C, T> component, BiConsumer<C, E> consumer) {
            this.component = component;
            this.consumer = consumer;
        }

        protected abstract void accept(E event, T object);

    }

    protected static class SimpleListener<C, E, T extends ComponentTarget<? super T>> extends Listener<C, E, T> {

        protected SimpleListener(ComponentType<C, T> component, BiConsumer<C, E> consumer) {
            super(component, consumer);
        }

        @Override
        protected void accept(E event, T object) {
            final var comp = object.getComponent(component);
            if (comp != null) consumer.accept(comp, event);
        }

    }

    protected static class FilteredListener<C, E, T extends ComponentTarget<? super T>> extends Listener<C, E, T> {

        private final EventFilter<E, T> filter;

        protected FilteredListener(ComponentType<C, T> component, BiConsumer<C, E> consumer, EventFilter<E, T> filter) {
            super(component, consumer);
            this.filter = filter;
        }

        @Override
        protected void accept(E event, T object) {
            if (filter.test(event, object)) {
                final var comp = object.getComponent(component);
                if (comp != null) consumer.accept(comp, event);
            }
        }

    }

}
