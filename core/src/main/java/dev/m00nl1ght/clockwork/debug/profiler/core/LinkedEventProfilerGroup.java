package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.EventType;
import dev.m00nl1ght.clockwork.core.TargetType;

public class LinkedEventProfilerGroup<E, T extends ComponentTarget> extends EventProfilerGroup<E, T> {

    private final EventProfilerGroup<E, ? super T> parent;

    public LinkedEventProfilerGroup(String name, TargetType<T> targetType, EventType<E, ? super T> eventType, EventProfilerGroup<E, ? super T> parent) {
        super(name, targetType);
        this.parent = parent;
        final var listeners = eventType.getListeners(targetType);
        this.listenerEntries = new EventProfilerEntry[listeners.size()];
        final var splitIdx = parent.listenerEntries.length;

        for (int i = 0; i < splitIdx; i++) {
            listenerEntries[i] = new LinkedEventProfilerEntry<>(eventType, targetType, listeners.get(i), parent.get(i));
        }

        for (int i = splitIdx; i < listenerEntries.length; i++) {
            listenerEntries[i] = new EventProfilerEntry<>(eventType, targetType, listeners.get(i));
        }
    }

}
