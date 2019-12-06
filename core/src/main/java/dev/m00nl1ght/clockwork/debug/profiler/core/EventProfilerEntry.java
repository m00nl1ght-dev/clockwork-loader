package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.EventType;
import dev.m00nl1ght.clockwork.debug.profiler.SimpleCyclicProfilerEntry;

public class EventProfilerEntry<T extends ComponentTarget> extends SimpleCyclicProfilerEntry {

    public static final int CAPACITY = 100;

    protected final ComponentType<?, T> componentType;

    public EventProfilerEntry(EventType<?, T> eventType, ComponentType<?, T> componentType) {
        super(componentType.getId(), CAPACITY);
        this.componentType = componentType;
    }

    public static class WithParent<T extends ComponentTarget> extends EventProfilerEntry<T> {

        private final EventProfilerEntry<? super T> parent;

        public WithParent(EventType<?, T> eventType, ComponentType<?, T> componentType, EventProfilerEntry<? super T> parent) {
            super(eventType, componentType);
            this.parent = parent;
        }

        @Override
        public void put(int value) {
            super.put(value);
            parent.put(value);
        }

    }

}
