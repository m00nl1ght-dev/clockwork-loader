package dev.m00nl1ght.clockwork.debug.profiler;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.listener.EventListener;

import java.util.function.BiConsumer;

public class EventProfilerEntry<E extends Event, T extends ComponentTarget, C> extends SimpleCyclicProfilerEntry implements BiConsumer<C, E> {

    private final EventListener<E, T, C> listener;
    private final BiConsumer<C, E> consumer;

    public EventProfilerEntry(EventListener<E, T, C> listener, int capacity) {
        super(listener.getComponentType().toString() + "[" + listener.getPriority() + "]", capacity);
        this.listener = listener;
        this.consumer = listener.getConsumer();
    }

    @Override
    public void accept(C component, E event) {
        final long t = System.nanoTime();
        consumer.accept(component, event);
        this.put(System.nanoTime() - t);
    }

}
