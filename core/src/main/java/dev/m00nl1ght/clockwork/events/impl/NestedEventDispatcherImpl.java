package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventDispatcher;
import dev.m00nl1ght.clockwork.events.NestedEventDispatcher;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.NestedEventListener;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NestedEventDispatcherImpl<E extends Event, T extends ComponentTarget, O extends ComponentTarget> implements NestedEventDispatcher<E, T, O> {

    protected final EventDispatcher<E, O> origin;
    protected final ComponentType<T, O> componentOrigin;
    protected final TargetType<T> targetType;

    public NestedEventDispatcherImpl(EventDispatcher<E, O> origin, TargetType<T> targetType, ComponentType<T, O> componentOrigin) {
        this.origin = Objects.requireNonNull(origin);
        this.componentOrigin = Objects.requireNonNull(componentOrigin);
        this.targetType = Objects.requireNonNull(targetType);
    }

    @Override
    public E post(T object, E event) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends T> List<EventListener<E, S, ?>> getListeners(TargetType<S> target) {
        if (target.getRoot() != targetType) checkCompatibility(target);
        return streamListeners()
                .filter(l -> l.getComponentType().getTargetType() == target)
                .map(l -> (EventListener<E, S, ?>) l)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends T> List<EventListener<E, ? super S, ?>> getEffectiveListeners(TargetType<S> target) {
        if (target.getRoot() != targetType) checkCompatibility(target);
        return streamListeners()
                .filter(l -> target.isEquivalentTo(l.getComponentType().getTargetType()))
                .map(l -> (EventListener<E, ? super S, ?>) l)
                .collect(Collectors.toUnmodifiableList());
    }

    private Stream<EventListener> streamListeners() {
        return origin.getListeners(componentOrigin.getTargetType()).stream()
                .filter(l -> l instanceof NestedEventListener)
                .map(l -> (NestedEventListener) l)
                .filter(l -> l.getComponentType() == componentOrigin)
                .map(NestedEventListener::getInnerListener);
    }

    protected <C> NestedEventListener<E, O, T, C> buildNestedListener(EventListener<E, ? extends T, C> listener) {
        final var target = listener.getComponentType().getTargetType();
        if (target.getRoot() != targetType) checkCompatibility(target);
        return new NestedEventListener<>(listener, componentOrigin);
    }

    @Override
    public void addListeners(Collection<EventListener<E, ? extends T, ?>> eventListeners) {
        origin.addListeners(eventListeners.stream()
                .map(this::buildNestedListener)
                .collect(Collectors.toUnmodifiableList()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeListeners(Collection<EventListener<E, ? extends T, ?>> eventListeners) {
        origin.removeListeners(origin.getListeners(componentOrigin.getTargetType()).stream()
                .filter(l -> l instanceof NestedEventListener).map(l -> (NestedEventListener<E, O, ?, ?>) l)
                .filter(l -> l.getComponentType() == componentOrigin && eventListeners.contains(l.getInnerListener()))
                .collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public EventDispatcher<E, O> getOrigin() {
        return origin;
    }

    @Override
    public ComponentType<T, O> getComponentOrigin() {
        return componentOrigin;
    }

    @Override
    public TargetType<T> getTargetType() {
        return targetType;
    }

    @Override
    public Collection<TargetType<? extends T>> getCompatibleTargetTypes() {
        return targetType.getAllSubtargets();
    }

    protected void checkCompatibility(TargetType<?> otherType) {
        if (!otherType.isEquivalentTo(targetType)) {
            final var msg = "Target " + otherType + " is not compatible with event dispatcher " + this;
            throw new IllegalArgumentException(msg);
        }
    }

}
