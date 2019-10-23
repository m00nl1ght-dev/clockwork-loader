package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;

import java.util.function.BiConsumer;

public final class EventListener<C, E, T> {

    private final EventType<E, T> eventType;
    private final ComponentType<C, T> component;
    private final BiConsumer<C, E> consumer;
    EventListener<?, E, T> next;

    EventListener(EventType<E, T> eventType, ComponentType<C, T> component, BiConsumer<C, E> consumer) {
        this.eventType = eventType;
        this.component = component;
        this.consumer = consumer;
    }

    @SuppressWarnings("unchecked")
    public void accept(E event, T object) {
        final var comp = ((ComponentTarget<T>) object).getComponent(component);
        if (comp != null) consumer.accept(comp, event);
    }

}
