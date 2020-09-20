package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.listener.CoreEventListener;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CoreEventType<E extends Event, O extends ComponentTarget> extends EventType<E, ClockworkCore> {

    protected final EventType<E, O> origin;
    protected final ComponentType<O, O> originIdentity;
    protected final ClockworkCore core;

    public CoreEventType(EventType<E, O> origin, ClockworkCore core) {
        super(Arguments.notNull(origin, "origin").getEventClassType(),
                Arguments.notNull(core, "core").getTargetType());
        this.originIdentity = origin.getTargetType().getIdentityComponentType();
        this.origin = origin;
        this.core = core;
    }

    @Override
    public E post(ClockworkCore object, E event) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends ClockworkCore> List<EventListener<E, S, ?>> getListeners(TargetType<S> target) {
        if (target != targetType) checkCompatibility(target);
        return origin.getListeners(originIdentity.getTargetType()).stream()
                .filter(l -> l instanceof CoreEventListener)
                .map(l -> (CoreEventListener) l)
                .filter(l -> l.getComponentType() == originIdentity)
                .map(l -> (EventListener<E, S, ?>) l.getInnerListener())
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
                .filter(l -> l instanceof CoreEventListener).map(l -> (CoreEventListener) l)
                .filter(l -> l.getComponentType() == originIdentity && eventListeners.contains(l.getInnerListener()))
                .collect(Collectors.toUnmodifiableList()));
    }

    @Override
    protected void checkCompatibility(TargetType<?> otherType) {
        if (otherType != targetType) {
            final var msg = "Cannot post event [] to different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, this, otherType));
        }
    }

}
