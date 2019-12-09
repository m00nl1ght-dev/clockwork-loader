package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.EventType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.SimpleCyclicProfilerEntry;

public class EventProfilerEntry<E, T extends ComponentTarget> extends SimpleCyclicProfilerEntry {

    public static final int CAPACITY = 100;

    public EventProfilerEntry(EventType<E, ? super T> eventType, TargetType<T> targetType, ComponentType<?, ? super T> componentType) {
        super(componentType.getId(), CAPACITY);
    }

}
