package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.NestedEventListener;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractNestedEventDispatcher<E extends Event, T extends ComponentTarget, O extends ComponentTarget> implements NestedEventDispatcher<E, T, O> {

    protected final EventDispatcher<E, O> origin;
    protected final ComponentType<T, O> componentOrigin;
    protected final TargetType<T> targetType;

    protected AbstractNestedEventDispatcher(EventDispatcher<E, O> origin, ComponentType<T, O> componentOrigin, TargetType<T> targetType) {
        this.origin = Arguments.notNull(origin, "origin");
        this.componentOrigin = Arguments.notNull(componentOrigin, "componentOrigin");
        this.targetType = Arguments.notNull(targetType, "targetType");
    }

    @Override
    public E post(T object, E event) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends T> List<EventListener<E, S, ?>> getListeners(TargetType<S> target) {
        return getRawListeners(target);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<EventListener<E, ? extends T, ?>> getEffectiveListeners(TargetType<? extends T> target) {
        return getRawListeners(target);
    }

    protected abstract List getRawListeners(TargetType<?> target);

    @Override
    public void addListeners(Collection<EventListener<E, ? extends T, ?>> eventListeners) {
        origin.addListeners(eventListeners.stream()
                .map(this::buildNestedListener)
                .collect(Collectors.toUnmodifiableList()));
    }

    protected abstract <I> NestedEventListener<E, O, T, I> buildNestedListener(EventListener<E, ? extends T, I> listener);

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

    protected void checkCompatibility(TargetType<?> otherType) {
        if (otherType != targetType) {
            final var msg = "Cannot post event [] to different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, this, otherType));
        }
    }

}
