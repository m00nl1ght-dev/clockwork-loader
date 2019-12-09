package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.EventType;
import dev.m00nl1ght.clockwork.core.TargetType;

public class LinkedEventProfilerGroup<T extends ComponentTarget> extends EventProfilerGroup<T> {

    private final EventProfilerGroup<? super T> parent;

    public LinkedEventProfilerGroup(String name, TargetType<T> targetType, EventProfilerGroup<? super T> parent) {
        super(name, targetType);
        this.parent = parent;
    }

    @Override
    protected void init(EventType<?, T> eventType) {
        final var listeners = eventType.getListeners(targetType);
        this.listenerEntries = new EventProfilerEntry[listeners.size()];
        final var splitIdx = parent.listenerEntries.length;

        for (int i = 0; i < splitIdx; i++) {
            listenerEntries[i] = new LinkedEventProfilerEntry<>(eventType, listeners.get(i), parent.get(i));
        }

        for (int i = splitIdx; i < listenerEntries.length; i++) {
            listenerEntries[i] = new EventProfilerEntry<>(eventType, listeners.get(i));
        }
    }

}
