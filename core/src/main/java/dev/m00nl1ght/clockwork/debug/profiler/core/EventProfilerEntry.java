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

}
