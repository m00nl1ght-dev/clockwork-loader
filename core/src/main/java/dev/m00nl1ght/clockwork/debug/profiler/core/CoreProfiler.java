package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.*;
import dev.m00nl1ght.clockwork.debug.profiler.DebugProfiler;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerGroup;

import java.util.List;

public class CoreProfiler extends DebugProfiler {

    protected final ClockworkCore core;
    protected TargetProfilerGroup[] ttGroups;

    protected CoreProfiler(ClockworkCore core) {
        this.core = core;
        if (core.getState() == ClockworkCore.State.CONSTRUCTED) throw new IllegalStateException();
    }

    public CoreProfiler(ClockworkCore core, String coreGroupName) {
        this(core);
        this.addGroup(new CoreGroup(coreGroupName));
        final var types = core.getRegisteredTargetTypes();
        this.ttGroups = new TargetProfilerGroup[types.size()];
        for (var type : types) ttGroups[type.getInternalIdx()] = buildGroup(type);
    }

    protected <T extends ComponentTarget> TargetProfilerGroup<T> buildGroup(TargetType<T> targetType) {
        final var parent = targetType.getParent() == null ? null : getGroupFor(targetType.getParent());
        return new TargetProfilerGroup<>(targetType, parent);
    }

    public <E, T extends ComponentTarget> EventProfilerGroup<E, T> getEntryFor(EventType<E, ? super T> eventType, TargetType<T> targetType) {
        return getGroupFor(targetType).getGroupFor(eventType);
    }

    public <F, T extends ComponentTarget> SubtargetProfilerGroup<T, F> getEntryFor(FunctionalSubtarget<? super T, F> subtarget, TargetType<T> targetType) {
        return getGroupFor(targetType).getGroupFor(subtarget);
    }

    @SuppressWarnings("unchecked")
    public <T extends ComponentTarget> TargetProfilerGroup<T> getGroupFor(TargetType<T> targetType) {
        return ttGroups[targetType.getInternalIdx()];
    }

    private class CoreGroup extends ProfilerGroup {

        public CoreGroup(String name) {
            super(name);
        }

        @Override
        public List<ProfilerGroup> getGroups() {
            return List.of(ttGroups);
        }

    }

}
