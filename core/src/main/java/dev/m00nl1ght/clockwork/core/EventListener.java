package dev.m00nl1ght.clockwork.core;

import java.util.function.BiConsumer;

public class EventListener<E, C, T extends ComponentTarget> {

    protected final ComponentType<C, T> component;
    protected final BiConsumer<C, E> consumer;

    public EventListener(ComponentType<C, T> component, BiConsumer<C, E> consumer) {
        this.component = component;
        this.consumer = consumer;
    }

    public void accept(T object, C component, E event) {
        consumer.accept(component, event);
    }

    public ComponentType<C, T> getComponentType() {
        return component;
    }

    public BiConsumer<C, E> getConsumer() {
        return consumer;
    }

    public EventFilter<E, C, T> getFilter() {
        return null;
    }

    public static class Filtered<E, C, T extends ComponentTarget> extends EventListener<E, C, T> {

        protected final EventFilter<E, C, T> filter;

        protected Filtered(ComponentType<C, T> component, BiConsumer<C, E> consumer, EventFilter<E, C, T> filter) {
            super(component, consumer);
            this.filter = filter;
        }

        @Override
        public void accept(T object, C component, E event) {
            if (filter.test(event, component, object)) {
                super.accept(object, component, event);
            }
        }

        @Override
        public EventFilter<E, C, T> getFilter() {
            return filter;
        }

    }

}
