package dev.m00nl1ght.clockwork.core;

import java.util.function.BiConsumer;

public interface EventListenerFactory {

    EventListenerFactory DEFAULT = new Default();

    <E, C, T extends ComponentTarget> EventListener<E, C, T> build(ComponentType<C, T> component, Class<E> eventClass, BiConsumer<C, E> consumer, EventFilter<E, C, T> filter);

    class Default implements EventListenerFactory {

        private Default() {}

        @Override
        public <E, C, T extends ComponentTarget> EventListener<E, C, T> build(ComponentType<C, T> component, Class<E> eventClass, BiConsumer<C, E> consumer, EventFilter<E, C, T> filter) {
            if (filter == null) {
                return new EventListener<>(component, consumer);
            } else {
                return new EventListener.Filtered<>(component, consumer, filter);
            }
        }

    }

}
