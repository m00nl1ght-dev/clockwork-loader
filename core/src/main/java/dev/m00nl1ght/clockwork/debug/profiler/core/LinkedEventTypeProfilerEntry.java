package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventType;

public class LinkedEventTypeProfilerEntry<E extends Event, T extends ComponentTarget> extends EventTypeProfilerEntry<E, T> {

    private final EventTypeProfilerEntry<E, ? super T> parent;

    public LinkedEventTypeProfilerEntry(EventType<E, ? super T> eventType, TargetType<T> targetType, ComponentType<?, ? extends T> componentType, EventTypeProfilerEntry<E, ? super T> parent) {
        super(eventType, targetType, componentType);
        this.parent = parent;
    }

    @Override
    public void put(int value) {
        super.put(value);
        parent.put(value);
    }

}
