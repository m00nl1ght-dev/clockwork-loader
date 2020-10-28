package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.debug.profiler.EventBusProfilerGroup;
import dev.m00nl1ght.clockwork.debug.profiler.Profilable;
import dev.m00nl1ght.clockwork.events.AbstractEventBus;
import dev.m00nl1ght.clockwork.events.EventDispatcher;
import dev.m00nl1ght.clockwork.events.NestedEventDispatcher;
import dev.m00nl1ght.clockwork.events.StaticEventDispatcher;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Objects;
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
            return attachProfiler(new ExactEventDispatcherImpl<>(eventType, target));
        } else {
            return attachProfiler(new EventDispatcherImpl<>(eventType, target));
        }
    }

    @Override
    protected <E extends ContextAwareEvent, O extends ComponentTarget, T extends ComponentTarget>
    NestedEventDispatcher<E, T, O> buildNestedDispatcher(TypeRef<E> eventType, Class<T> targetClass, Class<O> originClass) {
        final var origin = getEventDispatcher(eventType, originClass);
        final var target = core.getTargetType(targetClass)
                .orElseThrow(() -> FormatUtil.illArgExc("No target registered for class []", targetClass));
        final var originComponent = origin.getTargetType().getOwnComponentType(targetClass)
                .orElseThrow(() -> FormatUtil.illArgExc("No component registered for class [] in origin []", targetClass, origin.getTargetType()));
        return attachProfiler(new NestedEventDispatcherImpl<>(origin, originComponent, target));
    }

    @Override
    protected <E extends ContextAwareEvent, O extends ComponentTarget, T extends ComponentTarget>
    StaticEventDispatcher<E, T, O> buildStaticDispatcher(TypeRef<E> eventType, Class<T> targetClass, Class<O> originClass, T target) {
        final var origin = getEventDispatcher(eventType, originClass);
        return attachProfiler(new StaticEventDispatcherImpl<>(origin, target));
    }

    @Override
    public void attachProfiler(EventBusProfilerGroup profilerGroup) {
        Objects.requireNonNull(profilerGroup);
        this.profilerGroup = profilerGroup;
        getEventDispatchers().forEach(profilerGroup::attachToDispatcher);
    }

    private <D extends EventDispatcher<?, ?>> D attachProfiler(D dispatcher) {
        if (profilerGroup != null) profilerGroup.attachToDispatcher(dispatcher);
        return dispatcher;
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
