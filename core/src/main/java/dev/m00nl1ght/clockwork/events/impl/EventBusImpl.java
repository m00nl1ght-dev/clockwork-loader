package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.debug.profiler.EventBusProfilerGroup;
import dev.m00nl1ght.clockwork.debug.profiler.Profilable;
import dev.m00nl1ght.clockwork.events.AbstractEventBus;
import dev.m00nl1ght.clockwork.events.EventDispatcher;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Set;

public class EventBusImpl extends AbstractEventBus<ContextAwareEvent> {

    protected EventBusProfilerGroup profilerGroup;

    public EventBusImpl(ClockworkCore core) {
        super(core);
    }

    @Override
    protected <E extends ContextAwareEvent, T extends ComponentTarget>
    EventDispatcher<E, T> buildDispatcher(TypeRef<E> eventType, Class<T> targetClass) {
        final var target = core.getTargetType(targetClass)
                .orElseThrow(() -> FormatUtil.illArgExc("No target registered for class []", targetClass));
        if (target.getDirectSubtargets().isEmpty()) {
            return new ExactEventDispatcherImpl<>(eventType, target);
        } else {
            return new EventDispatcherImpl<>(eventType, target);
        }
    }

    @Override
    public void attachProfiler(EventBusProfilerGroup profilerGroup) {
        Arguments.notNull(profilerGroup, "profilerGroup");
        this.profilerGroup = profilerGroup;
        getEventDispatchers().forEach(profilerGroup::attachToDispatcher);
    }

    @Override
    public Set<? extends EventBusProfilerGroup> attachDefaultProfilers() {
        final var group = new EventBusProfilerGroup("EventBus", this);
        this.attachProfiler(group);
        return Set.of(group);
    }

    @Override
    public void detachAllProfilers() {
        if (this.profilerGroup == null) return;
        this.profilerGroup = null;
        getEventDispatchers().forEach(Profilable::detachAllProfilers);
    }

    @Override
    public boolean supportsProfilers() {
        return true;
    }

}
