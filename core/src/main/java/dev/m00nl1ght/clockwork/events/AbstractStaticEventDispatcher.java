package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.StaticEventListener;
import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractStaticEventDispatcher<E extends Event, T extends ComponentTarget, O extends ComponentTarget> implements StaticEventDispatcher<E, T, O> {

    protected final EventDispatcher<E, O> origin;
    protected final ComponentType<O, O> originIdentity;
    protected final TargetType<T> targetType;
    protected final T target;

    protected AbstractStaticEventDispatcher(EventDispatcher<E, O> origin, T target) {
        this.origin = Objects.requireNonNull(origin);
        this.originIdentity = origin.getTargetType().getIdentityComponentType();
        this.target = Objects.requireNonNull(target);
        this.targetType = ComponentTarget.typeOf(target);
    }

    @Override
    public E post(T target, E event) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends T> List<EventListener<E, S, ?>> getListeners(TargetType<S> target) {
        return getRawListeners(target);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends T> List<EventListener<E, ? super S, ?>> getEffectiveListeners(TargetType<S> target) {
        return getRawListeners(target);
    }

    protected abstract List getRawListeners(TargetType<?> target);

    @Override
    public void addListeners(Collection<EventListener<E, ? extends T, ?>> eventListeners) {
        origin.addListeners(eventListeners.stream()
                .map(this::buildStaticListener)
                .collect(Collectors.toUnmodifiableList()));
    }

    protected abstract <C> StaticEventListener<E, O, T, C> buildStaticListener(EventListener<E, ? extends T, C> listener);

    @Override
    @SuppressWarnings("unchecked")
    public void removeListeners(Collection<EventListener<E, ? extends T, ?>> eventListeners) {
        origin.removeListeners(origin.getListeners(originIdentity.getTargetType()).stream()
                .filter(l -> l instanceof StaticEventListener).map(l -> (StaticEventListener<E, O, T, ?>) l)
                .filter(l -> l.getComponentType() == originIdentity && eventListeners.contains(l.getInnerListener()))
                .collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public EventDispatcher<E, O> getOrigin() {
        return origin;
    }

    @Override
    public TargetType<T> getTargetType() {
        return targetType;
    }

    @Override
    public T getTarget() {
        return target;
    }

    protected void checkCompatibility(TargetType<?> otherType) {
        if (otherType != targetType) {
            final var msg = "Cannot post event [] to different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, this, otherType));
        }
    }

}
