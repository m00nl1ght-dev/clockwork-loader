package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.EventType;
import dev.m00nl1ght.clockwork.core.FunctionalSubtarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerGroup;

import java.util.List;

public class TargetProfilerGroup<T extends ComponentTarget> extends ProfilerGroup {

    protected final TargetType<T> targetType;
    protected EventProfilerGroup[] eventEntries;
    protected SubtargetProfilerGroup[] subtargetEntries;

    protected TargetProfilerGroup(TargetType<T> targetType) {
        super(targetType.getId());
        this.targetType = targetType;
        if (!targetType.isInitialised()) throw new IllegalStateException();
    }

    public TargetProfilerGroup(TargetType<T> targetType, TargetProfilerGroup<? super T> parent) {
        this(targetType);

        final var events = targetType.getEventTypes();
        this.eventEntries = new EventProfilerGroup[events.size()];
        for (var eventType : events) {
            eventEntries[eventType.getInternalId()] = buildGroup(targetType, eventType, parent);
        }

        final var subtargets = targetType.getSubtargets();
        this.subtargetEntries = new SubtargetProfilerGroup[events.size()];
        for (var subtarget : subtargets) {
            subtargetEntries[subtarget.getInternalId()] = buildGroup(targetType, subtarget, parent);
        }
    }

    protected static <E, U extends ComponentTarget, T extends U> EventProfilerGroup<E, T> buildGroup(TargetType<T> targetType, EventType<E, T> eventType, TargetProfilerGroup<U> parent) {
        final var name = eventType.getEventClass().getSimpleName();
        if (parent != null && eventType.getInternalId() < parent.eventEntries.length) {
            final var pEvt = parent.targetType.getEventType(eventType.getEventClass());
            return new LinkedEventProfilerGroup<>(name, targetType, eventType, parent.getGroupFor(pEvt));
        } else {
            return new EventProfilerGroup<>(name, targetType, eventType);
        }
    }

    protected static <F, U extends ComponentTarget, T extends U> SubtargetProfilerGroup<T, F> buildGroup(TargetType<T> targetType, FunctionalSubtarget<T, F> subtarget, TargetProfilerGroup<U> parent) {
        final var name = subtarget.getType().getSimpleName();
        if (parent != null && subtarget.getInternalId() < parent.eventEntries.length) {
            final var pSbt = parent.targetType.getSubtarget(subtarget.getType());
            return new LinkedSubtargetProfilerGroup<>(name, targetType, subtarget, parent.getGroupFor(pSbt));
        } else {
            return new SubtargetProfilerGroup<>(name, targetType, subtarget);
        }
    }

    @Override
    public List<ProfilerGroup> getGroups() {
        return List.of(eventEntries);
    }

    @SuppressWarnings("unchecked")
    public <E> EventProfilerGroup<E, T> getGroupFor(EventType<E, ? super T> eventType) {
        return eventEntries[eventType.getInternalId()];
    }

    @SuppressWarnings("unchecked")
    public <F> SubtargetProfilerGroup<T, F> getGroupFor(FunctionalSubtarget<? super T, F> subtarget) {
        return subtargetEntries[subtarget.getInternalId()];
    }

}
