package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.EventType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerGroup;

import java.util.List;

public class TargetProfilerGroup<T extends ComponentTarget> extends ProfilerGroup {

    protected final TargetType<T> targetType;
    protected EventProfilerGroup[] eventEntries;

    public TargetProfilerGroup(TargetType<T> targetType) {
        super(targetType.getId());
        this.targetType = targetType;
    }

    protected void init(TargetProfilerGroup<? super T> parent) {
        if (!targetType.isInitialised()) throw new IllegalStateException();
        final var events = targetType.getEventTypes();
        this.eventEntries = new EventProfilerGroup[events.size()];
        for (var eventType : events) {
            final var group = buildGroup(eventType, parent);
            eventEntries[eventType.getInternalId()] = group;
            group.init(eventType);
        }
    }

    private EventProfilerGroup<T> buildGroup(EventType<?, T> eventType, TargetProfilerGroup<? super T> parent) {
        final var name = eventType.getEventClass().getSimpleName();
        if (parent != null && eventType.getInternalId() < parent.eventEntries.length) {
            return new LinkedEventProfilerGroup<>(name, targetType, parent.get(eventType.getInternalId()));
        } else {
            return new EventProfilerGroup<>(name, targetType);
        }
    }

    @Override
    public List<ProfilerGroup> getGroups() {
        return List.of(eventEntries);
    }

    @SuppressWarnings("unchecked")
    public EventProfilerGroup<T> get(int idx) {
        return eventEntries[idx];
    }

}
