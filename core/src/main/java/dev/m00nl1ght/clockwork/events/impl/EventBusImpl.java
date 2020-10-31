package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.EventBusProfilerGroup;
import dev.m00nl1ght.clockwork.debug.profiler.Profilable;
import dev.m00nl1ght.clockwork.events.AbstractEventBus;
import dev.m00nl1ght.clockwork.events.EventDispatcher;
import dev.m00nl1ght.clockwork.events.NestedEventDispatcher;
import dev.m00nl1ght.clockwork.events.StaticEventDispatcher;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.EventListenerPriority;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

public class EventBusImpl extends AbstractEventBus<ContextAwareEvent> {

    protected final ClockworkCore core;
    protected EventBusProfilerGroup profilerGroup;

    public EventBusImpl(ClockworkCore core) {
        this.core = Objects.requireNonNull(core);
        core.getState().requireOrAfter(ClockworkCore.State.PROCESSED);
    }

    public <E extends ContextAwareEvent, O extends ComponentTarget>
    StaticEventDispatcher<E, ClockworkCore, O> getStaticEventDispatcher(TypeRef<E> eventType, Class<O> originClass) {
        return getStaticEventDispatcher(eventType, ClockworkCore.class, originClass, core);
    }

    public <E extends ContextAwareEvent, O extends ComponentTarget>
    StaticEventDispatcher<E, ClockworkCore, O> getStaticEventDispatcher(Class<E> eventClass, Class<O> originClass) {
        return getStaticEventDispatcher(TypeRef.of(eventClass), originClass);
    }

    @Override
    public <E extends ContextAwareEvent, T extends ComponentTarget, C>
    EventListener<E, ? extends T, C> addListener(TypeRef<E> eventType, Class<T> targetClass, Class<C> componentClass, BiConsumer<C, E> consumer, EventListenerPriority priority) {
        final var dispatcher = getEventDispatcher(eventType, targetClass);
        return dispatcher.addListener(findComponentType(dispatcher.getTargetType(), componentClass), consumer, priority);
    }

    @Override
    public <E extends ContextAwareEvent, O extends ComponentTarget, T extends ComponentTarget, C>
    EventListener<E, T, C> addNestedListener(TypeRef<E> eventType, Class<T> targetClass, Class<O> originClass, Class<C> componentClass, BiConsumer<C, E> consumer, EventListenerPriority priority) {
        final var dispatcher = getNestedEventDispatcher(eventType, targetClass, originClass);
        return dispatcher.addListener(findComponentType(dispatcher.getTargetType(), componentClass), consumer, priority);
    }

    @Override
    public <E extends ContextAwareEvent, O extends ComponentTarget, T extends ComponentTarget, C>
    EventListener<E, T, C> addStaticListener(TypeRef<E> eventType, Class<T> targetClass, Class<O> originClass, T target, Class<C> componentClass, BiConsumer<C, E> consumer, EventListenerPriority priority) {
        final var dispatcher = getStaticEventDispatcher(eventType, targetClass, originClass, target);
        return dispatcher.addListener(findComponentType(dispatcher.getTargetType(), componentClass), consumer, priority);
    }

    private <T extends ComponentTarget, C>
    ComponentType<C, ? extends T> findComponentType(TargetType<T> targetType, Class<C> componentClass) {
        final var component = core.getComponentType(componentClass);
        if (component.isEmpty()) {
            throw new IllegalArgumentException("No component type registered for class: " + componentClass);
        } else if (!component.get().getTargetType().isEquivalentTo(targetType)) {
            throw new IllegalArgumentException("Component type " + component + " is not compatible with target type " + targetType);
        } else {
            @SuppressWarnings("unchecked")
            final var casted = (ComponentType<C, ? extends T>) component.get();
            return casted;
        }
    }

    public <E extends ContextAwareEvent, O extends ComponentTarget, C>
    EventListener<E, ClockworkCore, C> addStaticListener(TypeRef<E> eventType, Class<O> originClass, Class<C> componentClass, BiConsumer<C, E> consumer, EventListenerPriority priority) {
        return addStaticListener(eventType, ClockworkCore.class, originClass, core, componentClass, consumer, priority);
    }

    public <E extends ContextAwareEvent, O extends ComponentTarget, C>
    EventListener<E, ClockworkCore, C> addStaticListener(TypeRef<E> eventType, Class<O> originClass, Class<C> componentClass, BiConsumer<C, E> consumer) {
        return addStaticListener(eventType, ClockworkCore.class, originClass, core, componentClass, consumer, EventListenerPriority.NORMAL);
    }

    public <E extends ContextAwareEvent, O extends ComponentTarget, C>
    EventListener<E, ClockworkCore, C> addStaticListener(Class<E> eventClass, Class<O> originClass, Class<C> componentClass, BiConsumer<C, E> consumer, EventListenerPriority priority) {
        return addStaticListener(TypeRef.of(eventClass), ClockworkCore.class, originClass, core, componentClass, consumer, priority);
    }

    public <E extends ContextAwareEvent, O extends ComponentTarget, C>
    EventListener<E, ClockworkCore, C> addStaticListener(Class<E> eventClass, Class<O> originClass, Class<C> componentClass, BiConsumer<C, E> consumer) {
        return addStaticListener(TypeRef.of(eventClass), ClockworkCore.class, originClass, core, componentClass, consumer, EventListenerPriority.NORMAL);
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
                .orElseThrow(() -> FormatUtil.illArgExc("No component registered for class [] in target []", targetClass, origin.getTargetType()));
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

    public final ClockworkCore getCore() {
        return core;
    }

}
