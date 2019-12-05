package dev.m00nl1ght.clockwork.event.listener;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.debug.ProfilerEntry;
import dev.m00nl1ght.clockwork.event.filter.EventFilter;

import java.util.function.BiConsumer;

public class FilteredEventListener<E, C, T extends ComponentTarget> extends SimpleEventListener<E, C, T> {

    protected final EventFilter<E, C, T> filter;

    public FilteredEventListener(ComponentType<C, T> component, BiConsumer<C, E> consumer, EventFilter<E, C, T> filter) {
        super(component, consumer);
        this.filter = filter;
    }

    @Override
    public void accept(T object, C component, E event) {
        if (filter.test(event, component, object)) {
            consumer.accept(component, event);
        }
    }

    @Override
    public void accept(T object, C component, E event, ProfilerEntry profilerEntry) {
        if (filter.test(event, component, object)) {
            profilerEntry.start();
            consumer.accept(component, event);
            profilerEntry.end();
        }
    }

}
