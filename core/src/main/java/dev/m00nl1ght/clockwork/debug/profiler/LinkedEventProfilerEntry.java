package dev.m00nl1ght.clockwork.debug.profiler;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.listener.EventListener;

public class LinkedEventProfilerEntry<E extends Event, T extends ComponentTarget, C> extends EventProfilerEntry<E, T, C> {

    private final EventProfilerEntry<E, ?, ?> linked;

    public LinkedEventProfilerEntry(EventListener<E, T, C> listener, int capacity, EventProfilerEntry<E, ?, ?> linked) {
        super(listener, capacity);
        this.linked = linked;
    }

    @Override
    public void put(int value) {
        super.put(value);
        linked.put(value);
    }

}
