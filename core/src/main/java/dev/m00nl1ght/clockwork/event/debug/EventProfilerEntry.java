package dev.m00nl1ght.clockwork.event.debug;

import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.utils.profiler.impl.CyclicProfilerEntry;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class EventProfilerEntry<E extends Event, T extends ComponentTarget, C> extends CyclicProfilerEntry implements BiConsumer<C, E> {

    private static int CAPACITY = 100;

    public static void setCapacity(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException();
        EventProfilerEntry.CAPACITY = capacity;
    }

    private final EventListener<E, T, C> listener;
    private final BiConsumer<C, E> consumer;

    public EventProfilerEntry(@NotNull String name, @NotNull EventListener<E, T, C> listener) {
        super(name, CAPACITY);
        this.listener = listener;
        this.consumer = listener.getConsumer();
    }

    @Override
    public void accept(C component, E event) {
        final long t = System.nanoTime();
        consumer.accept(component, event);
        this.put(System.nanoTime() - t);
    }

    public @NotNull EventListener<E, T, C> getListener() {
        return listener;
    }

}
