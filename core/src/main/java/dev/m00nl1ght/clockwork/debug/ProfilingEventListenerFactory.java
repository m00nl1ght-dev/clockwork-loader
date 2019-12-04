package dev.m00nl1ght.clockwork.debug;

import dev.m00nl1ght.clockwork.core.*;

import java.util.function.BiConsumer;

public class ProfilingEventListenerFactory implements EventListenerFactory {

    private static final int PROFILER_CAPACITY = 50;

    private final DebugProfiler profiler;

    public ProfilingEventListenerFactory(DebugProfiler profiler) {
        this.profiler = profiler;
    }

    @Override
    public <E, C, T extends ComponentTarget> EventListener<E, C, T> build(ComponentType<C, T> component, Class<E> eventClass, BiConsumer<C, E> consumer, EventFilter<E, C, T> filter) {
        final var groupName = component.getTargetType().getId() + " << " + eventClass.getSimpleName();
        final var group = profiler.getOrCreateGroup(groupName);
        if (filter == null) {
            return new ProfilingListener<>(group, component, consumer);
        } else {
            return new FilteredProfilingListener<>(group, component, consumer, filter);
        }
    }

    public static class ProfilingListener<E, C, T extends ComponentTarget> extends EventListener<E, C, T> {

        private final ProfilerEntry entry;

        protected ProfilingListener(ProfilerGroup group, ComponentType<C, T> component, BiConsumer<C, E> consumer) {
            super(component, consumer);
            this.entry = new SimpleCyclicProfilerEntry(group, component.getId(), PROFILER_CAPACITY);
            this.entry.addDebugInfo("consumer", consumer.toString());
        }

        @Override
        public void accept(T object, C component, E event) {
            entry.start();
            consumer.accept(component, event);
            entry.end();
        }

    }

    public static class FilteredProfilingListener<E, C, T extends ComponentTarget> extends EventListener.Filtered<E, C, T> {

        private final ProfilerEntry entry;

        protected FilteredProfilingListener(ProfilerGroup group, ComponentType<C, T> component, BiConsumer<C, E> consumer, EventFilter<E, C, T> filter) {
            super(component, consumer, filter);
            this.entry = new SimpleCyclicProfilerEntry(group, component.getId(), PROFILER_CAPACITY);
            this.entry.addDebugInfo("consumer", consumer.toString());
            this.entry.addDebugInfo("filter", filter.toString());
        }

        @Override
        public void accept(T object, C component, E event) {
            if (filter.test(event, component, object)) {
                entry.start();
                consumer.accept(component, event);
                entry.end();
            }
        }

    }

}
