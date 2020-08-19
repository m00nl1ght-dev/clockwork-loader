package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventType;

// TODO

public class LinkedEventTypeProfilerGroup<E extends Event, T extends ComponentTarget> extends EventTypeProfilerGroup<E, T> {

    private final EventTypeProfilerGroup<E, ? super T> parent;

    public LinkedEventTypeProfilerGroup(String name, TargetType<T> targetType, EventType<E, ? super T> eventType, EventTypeProfilerGroup<E, ? super T> parent) {
        super(name, targetType);
        this.parent = parent;
        final var listeners = eventType.getListeners(targetType);
        this.listenerEntries = new EventTypeProfilerEntry[listeners.size()];
        final var splitIdx = parent.listenerEntries.length;

        for (int i = 0; i < splitIdx; i++) {
            // listenerEntries[i] = new LinkedEventTypeProfilerEntry<>(eventType, targetType, listeners.get(i), parent.get(i));
        }

        for (int i = splitIdx; i < listenerEntries.length; i++) {
            // listenerEntries[i] = new EventTypeProfilerEntry<>(eventType, targetType, listeners.get(i));
        }
    }

}
