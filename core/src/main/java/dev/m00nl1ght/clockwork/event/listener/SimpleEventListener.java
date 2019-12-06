package dev.m00nl1ght.clockwork.event.listener;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerEntry;

import java.util.function.BiConsumer;

public class SimpleEventListener<E, C, T extends ComponentTarget> implements EventListener<E, C, T> {

    protected final ComponentType<C, T> component;
    protected final BiConsumer<C, E> consumer;

    public SimpleEventListener(ComponentType<C, T> component, BiConsumer<C, E> consumer) {
        this.component = component;
        this.consumer = consumer;
    }

    public void accept(T object, C component, E event) {
        consumer.accept(component, event);
    }

    public void accept(T object, C component, E event, ProfilerEntry profilerEntry) {
        profilerEntry.start();
        consumer.accept(component, event);
        profilerEntry.end();
    }

    public ComponentType<C, T> getComponentType() {
        return component;
    }

}
