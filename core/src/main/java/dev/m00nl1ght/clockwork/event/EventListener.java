package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;

import java.util.function.BiConsumer;

public class EventListener<C, E, T extends ComponentTarget<T>> {

    private final EventType<E, T> eventType;
    private final ComponentType<C, T> component;
    private final BiConsumer<C, E> consumer;

    protected EventListener(EventType<E, T> eventType, ComponentType<C, T> component, BiConsumer<C, E> consumer) {
        this.eventType = eventType;
        this.component = component;
        this.consumer = consumer;
    }

    public void accept(E event, T object) {
        final var comp = object.getComponent(component);
        if (comp != null) consumer.accept(comp, event);
    }

}
