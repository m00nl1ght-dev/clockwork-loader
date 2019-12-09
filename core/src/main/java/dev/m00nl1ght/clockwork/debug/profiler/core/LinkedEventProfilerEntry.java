package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.EventType;

public class LinkedEventProfilerEntry<T extends ComponentTarget> extends EventProfilerEntry<T> {

    private final EventProfilerEntry<? super T> parent;

    public LinkedEventProfilerEntry(EventType<?, T> eventType, ComponentType<?, T> componentType, EventProfilerEntry<? super T> parent) {
        super(eventType, componentType);
        this.parent = parent;
    }

    @Override
    public void put(int value) {
        super.put(value);
        parent.put(value);
    }

}
