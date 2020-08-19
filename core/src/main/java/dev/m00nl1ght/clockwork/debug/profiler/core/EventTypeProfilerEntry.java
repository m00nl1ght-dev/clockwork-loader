package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.SimpleCyclicProfilerEntry;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventType;

public class EventTypeProfilerEntry<E extends Event, T extends ComponentTarget> extends SimpleCyclicProfilerEntry {

    public static final int CAPACITY = 100;

    public EventTypeProfilerEntry(EventType<E, ? super T> eventType, TargetType<T> targetType, ComponentType<?, ? extends T> componentType) {
        super(componentType.getId(), CAPACITY);
    }

}
