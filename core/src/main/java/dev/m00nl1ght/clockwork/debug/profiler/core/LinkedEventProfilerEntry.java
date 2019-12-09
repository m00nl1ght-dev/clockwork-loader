package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.EventType;
import dev.m00nl1ght.clockwork.core.TargetType;

public class LinkedEventProfilerEntry<E, T extends ComponentTarget> extends EventProfilerEntry<E, T> {

    private final EventProfilerEntry<E, ? super T> parent;

    public LinkedEventProfilerEntry(EventType<E, ? super T> eventType, TargetType<T> targetType, ComponentType<?, ? super T> componentType, EventProfilerEntry<E, ? super T> parent) {
        super(eventType, targetType, componentType);
        this.parent = parent;
    }

    @Override
    public void put(int value) {
        super.put(value);
        parent.put(value);
    }

}
