package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;

import java.util.function.BiConsumer;

public final class EventListener<C, E, T> {

    private final ComponentType<C, T> component;
    private final BiConsumer<C, E> consumer;
    EventListener<?, E, T> next;

    EventListener(ComponentType<C, T> component, BiConsumer<C, E> consumer) {
        this.component = component;
        this.consumer = consumer;
    }

    public void accept(E event, ComponentTarget<T> object) {
        final var comp = object.getComponent(component);
        if (comp != null) consumer.accept(comp, event);
    }

}
