package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerEntry;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerGroup;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventType;

import java.util.List;

public class EventTypeProfilerGroup<E extends Event, T extends ComponentTarget> extends ProfilerGroup {

    protected final TargetType<T> targetType;
    protected EventTypeProfilerEntry[] listenerEntries;

    protected EventTypeProfilerGroup(String name, TargetType<T> targetType) {
        super(name);
        this.targetType = targetType;
    }

    public EventTypeProfilerGroup(String name, TargetType<T> targetType, EventType<E, ? super T> eventType) {
        this(name, targetType);
        final var listeners = eventType.getListeners(targetType);
        this.listenerEntries = new EventTypeProfilerEntry[listeners.size()];
        for (int i = 0; i < listeners.size(); i++) {
            listenerEntries[i] = new EventTypeProfilerEntry<>(eventType, targetType, listeners.get(i));
        }
    }

    @Override
    public List<ProfilerEntry> getEntries() {
        return List.of(listenerEntries);
    }

    @SuppressWarnings("unchecked")
    public EventTypeProfilerEntry<E, T> get(int idx) {
        return listenerEntries[idx];
    }

}
