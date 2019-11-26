package dev.m00nl1ght.clockwork.core;

import java.util.function.BiConsumer;

public interface EventListenerFactory {

    EventListenerFactory DEFAULT = new Default();

    <C, E, T extends ComponentTarget> EventListener<E, T> build(ComponentType<C, T> component, Class<E> eventClass, BiConsumer<C, E> consumer, EventFilter<E, T> filter);

    class Default implements EventListenerFactory {

        private Default() {}

        @Override
        public <C, E, T extends ComponentTarget> EventListener<E, T> build(ComponentType<C, T> component, Class<E> eventClass, BiConsumer<C, E> consumer, EventFilter<E, T> filter) {
            if (filter == null) {
                return new EventListener.Base<>(component, consumer);
            } else {
                return new EventListener.Filtered<>(component, consumer, filter);
            }
        }

    }

}
