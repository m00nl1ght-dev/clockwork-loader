package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.EventType;
import dev.m00nl1ght.clockwork.debug.profiler.DebugProfiler;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerGroup;

import java.util.List;

public class CoreProfiler extends DebugProfiler {

    protected final ClockworkCore core;
    protected TargetTypeProfilerGroup[] ttGroups;

    public CoreProfiler(ClockworkCore core) {
        this.core = core;
        this.addGroup(new CoreGroup());
        this.init();
    }

    @SuppressWarnings("unchecked")
    protected void init() {
        if (core.getState() == ClockworkCore.State.LOCATED) throw new IllegalStateException();
        final var types = core.getRegisteredTargetTypes();
        this.ttGroups = new TargetTypeProfilerGroup[types.size()];
        for (var type : types) {
            final var group = new TargetTypeProfilerGroup<>(type);
            ttGroups[type.getInternalIdx()] = group;
            if (type.getParent() != null) {
                group.init(ttGroups[type.getParent().getInternalIdx()]);
            } else {
                group.init(null);
            }
        }

    }

    @SuppressWarnings("unchecked")
    public <E, T extends ComponentTarget> E postEvent(EventType<E, T> eventType, T object, E event) {
        final var ttpg = ttGroups[object.getComponentContainer().getTargetType().getInternalIdx()];
        return eventType.post(object, event, (EventTypeProfilerGroup<T>) ttpg.eventEntries[eventType.getInternalId()]);
    }

    private class CoreGroup extends ProfilerGroup {

        public CoreGroup() {
            super("core");
        }

        @Override
        public List<ProfilerGroup> getGroups() {
            return List.of(ttGroups);
        }

    }

}
