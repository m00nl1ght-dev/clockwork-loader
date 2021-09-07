package dev.m00nl1ght.clockwork.event.debug;

import dev.m00nl1ght.clockwork.utils.debug.profiler.ProfilerGroup;
import dev.m00nl1ght.clockwork.event.EventBus;
import dev.m00nl1ght.clockwork.event.EventDispatcher;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class EventBusProfilerGroup extends ProfilerGroup {

    protected final EventBus<?> eventBus;
    protected final Set<EventDispatcherProfilerGroup<?, ?>> groups = new LinkedHashSet<>();

    public EventBusProfilerGroup(String name, EventBus<?> eventBus) {
        super(name);
        this.eventBus = eventBus;
    }

    public void attachToDispatcher(EventDispatcher<?, ?> eventDispatcher) {
        this.groups.addAll(eventDispatcher.attachDefaultProfilers());
    }

    @Override
    public List<ProfilerGroup> getGroups() {
        return List.copyOf(groups);
    }

}
