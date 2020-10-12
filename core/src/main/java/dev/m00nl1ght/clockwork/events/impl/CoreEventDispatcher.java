package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventDispatcher;
import dev.m00nl1ght.clockwork.events.listener.CoreEventListener;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CoreEventDispatcher<E extends Event, O extends ComponentTarget> implements EventDispatcher<E, ClockworkCore> {

    protected final EventDispatcher<E, O> origin;
    protected final ComponentType<O, O> originIdentity;
    protected final TargetType<ClockworkCore> targetType;
    protected final ClockworkCore core;

    public CoreEventDispatcher(EventDispatcher<E, O> origin, ClockworkCore core) {
        this.origin = Arguments.notNull(origin, "origin");
        this.originIdentity = origin.getTargetType().getIdentityComponentType();
        this.core = Arguments.notNull(core, "core");
        this.targetType = core.getTargetType();
    }

    @Override
    public E post(ClockworkCore object, E event) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends ClockworkCore> List<EventListener<E, S, ?>> getListeners(TargetType<S> target) {
        return getRawListeners(target);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<EventListener<E, ? extends ClockworkCore, ?>> getEffectiveListeners(TargetType<? extends ClockworkCore> target) {
        return getRawListeners(target);
    }

    protected List getRawListeners(TargetType<?> target) {
        if (target != targetType) checkCompatibility(target);
        return origin.getListeners(originIdentity.getTargetType()).stream()
                .filter(l -> l instanceof CoreEventListener)
                .map(l -> (CoreEventListener) l)
                .filter(l -> l.getComponentType() == originIdentity)
                .map(CoreEventListener::getInnerListener)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void addListeners(Collection<EventListener<E, ? extends ClockworkCore, ?>> eventListeners) {
        origin.addListeners(eventListeners.stream()
                .map(this::buildCoreListener)
                .collect(Collectors.toUnmodifiableList()));
    }

    private <I> CoreEventListener<E, O, I> buildCoreListener(EventListener<E, ? extends ClockworkCore, I> listener) {
        final var target = listener.getComponentType().getTargetType();
        if (target != targetType) checkCompatibility(target);
        @SuppressWarnings("unchecked")
        final var casted = (EventListener<E, ClockworkCore, I>) listener;
        return new CoreEventListener<>(casted, originIdentity.getTargetType(), core);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeListeners(Collection<EventListener<E, ? extends ClockworkCore, ?>> eventListeners) {
        origin.removeListeners(origin.getListeners(originIdentity.getTargetType()).stream()
                .filter(l -> l instanceof CoreEventListener).map(l -> (CoreEventListener<E, O, ?>) l)
                .filter(l -> l.getComponentType() == originIdentity && eventListeners.contains(l.getInnerListener()))
                .collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public TypeRef<E> getEventClassType() {
        return origin.getEventClassType();
    }

    @Override
    public TargetType<ClockworkCore> getTargetType() {
        return targetType;
    }

    @Override
    public Collection<TargetType<? extends ClockworkCore>> getCompatibleTargetTypes() {
        return List.of(targetType);
    }

    protected void checkCompatibility(TargetType<?> otherType) {
        if (otherType != targetType) {
            final var msg = "Cannot post event [] to different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, this, otherType));
        }
    }

}
