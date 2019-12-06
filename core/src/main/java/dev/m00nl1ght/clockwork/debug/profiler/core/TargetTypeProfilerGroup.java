package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.EventType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerGroup;

import java.util.List;

public class TargetTypeProfilerGroup<T extends ComponentTarget> extends ProfilerGroup {

    protected final TargetType<T> targetType;
    protected EventTypeProfilerGroup[] eventEntries;

    public TargetTypeProfilerGroup(TargetType<T> targetType) {
        super(targetType.getId());
        this.targetType = targetType;
    }

    protected void init(TargetTypeProfilerGroup<? super T> parent) {
        if (!targetType.isInitialised()) throw new IllegalStateException();
        final var events = targetType.getEventTypes();
        this.eventEntries = new EventTypeProfilerGroup[events.size()];
        for (var eventType : events) {
            final var group = buildGroup(eventType, parent);
            eventEntries[eventType.getInternalId()] = group;
            group.init(eventType);
        }
    }

    private EventTypeProfilerGroup<T> buildGroup(EventType<?, T> eventType, TargetTypeProfilerGroup<? super T> parent) {
        final var name = eventType.getEventClass().getSimpleName();
        if (parent != null && eventType.getInternalId() < parent.eventEntries.length) {
            return new EventTypeProfilerGroup.WithParent<>(name, targetType, parent.get(eventType.getInternalId()));
        } else {
            return new EventTypeProfilerGroup<>(name, targetType);
        }
    }

    @Override
    public List<ProfilerGroup> getGroups() {
        return List.of(eventEntries);
    }

    @SuppressWarnings("unchecked")
    public EventTypeProfilerGroup<T> get(int idx) {
        return eventEntries[idx];
    }

}
