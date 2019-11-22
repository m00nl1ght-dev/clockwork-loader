package dev.m00nl1ght.clockwork.debug;

import dev.m00nl1ght.clockwork.core.*;

import java.util.function.BiConsumer;

public class ProfilingEventDispatcher<E, T extends ComponentTarget> extends EventDispatcher<E, T> {

    private static final int PROFILER_CAPACITY = 50;

    private final ProfilerGroup group;

    public ProfilingEventDispatcher(TargetType<T> target, Class<E> eventClass, ProfilerGroup profilerGroup) {
        super(target, eventClass);
        this.group = profilerGroup;
    }

    @Override
    protected <C> Listener<C, E, T> buildListener(ComponentType<C, T> componentType, BiConsumer<C, E> consumer, EventFilter<E, T> filter) {
        if (filter == null) {
            return new SimpleProfilingListener<>(componentType, consumer);
        } else {
            return new FilteredProfilingListener<>(componentType, consumer, filter);
        }
    }

    @SuppressWarnings("unchecked")
    protected class SimpleProfilingListener<C> extends SimpleListener<C, E, T> {

        private final ProfilerEntry entry;

        protected SimpleProfilingListener(ComponentType<C, T> component, BiConsumer<C, E> consumer) {
            super(component, consumer);
            this.entry = new SimpleCyclicProfilerEntry(group, component.getId(), PROFILER_CAPACITY);
            this.entry.addDebugInfo("consumer", consumer.toString());
        }

        @Override
        protected void accept(E event, T object) {
            entry.start();
            final var comp = object.getComponent(component.getInternalID());
            if (comp != null) consumer.accept((C) comp, event);
            entry.end();
        }

    }

    @SuppressWarnings("unchecked")
    protected class FilteredProfilingListener<C> extends FilteredListener<C, E, T> {

        private final ProfilerEntry entry;

        protected FilteredProfilingListener(ComponentType<C, T> component, BiConsumer<C, E> consumer, EventFilter<E, T> filter) {
            super(component, consumer, filter);
            this.entry = new SimpleCyclicProfilerEntry(group, component.getId(), PROFILER_CAPACITY);
            this.entry.addDebugInfo("consumer", consumer.toString());
            this.entry.addDebugInfo("filter", filter.toString());
        }

        @Override
        protected void accept(E event, T object) {
            if (filter.test(event, object)) {
                entry.start();
                final var comp = object.getComponent(component.getInternalID());
                if (comp != null) consumer.accept((C) comp, event);
                entry.end();
            }
        }

    }

    public static EventDispatcherFactory factory(DebugProfiler profiler) {
        return new Factory(profiler);
    }

    static class Factory implements EventDispatcherFactory {

        private final DebugProfiler profiler;
        Factory(DebugProfiler profiler) {this.profiler = profiler;}

        @Override
        public <E, T extends ComponentTarget> EventDispatcher<E, T> build(TargetType<T> targetType, Class<E> eventClass) {
            final var group = new ProfilerGroup(profiler, targetType.getId() + "/" + eventClass.getSimpleName());
            return new ProfilingEventDispatcher<>(targetType, eventClass, group);
        }

    }

}
