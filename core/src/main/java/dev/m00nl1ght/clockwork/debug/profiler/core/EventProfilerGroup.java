package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.EventType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerEntry;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerGroup;

import java.util.List;

public class EventProfilerGroup<E, T extends ComponentTarget> extends ProfilerGroup {

    protected final TargetType<T> targetType;
    protected EventProfilerEntry[] listenerEntries;

    protected EventProfilerGroup(String name, TargetType<T> targetType) {
        super(name);
        this.targetType = targetType;
    }

    public EventProfilerGroup(String name, TargetType<T> targetType, EventType<E, ? super T> eventType) {
        this(name, targetType);
        final var listeners = eventType.getListeners(targetType);
        this.listenerEntries = new EventProfilerEntry[listeners.size()];
        for (int i = 0; i < listeners.size(); i++) {
            listenerEntries[i] = new EventProfilerEntry<>(eventType, targetType, listeners.get(i));
        }
    }

    @Override
    public List<ProfilerEntry> getEntries() {
        return List.of(listenerEntries);
    }

    @SuppressWarnings("unchecked")
    public EventProfilerEntry<E, T> get(int idx) {
        return listenerEntries[idx];
    }

}
