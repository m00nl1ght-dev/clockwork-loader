package dev.m00nl1ght.clockwork.event.impl.listener;

import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.TargetType;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventListener;
import dev.m00nl1ght.clockwork.utils.logger.FormatUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class EventListenerForwardingByLambda<E extends Event, S extends ComponentTarget, D extends ComponentTarget, DL extends ComponentTarget, C> extends EventListener<E, S, S> {

    protected final EventListener<E, DL, C> innerListener;
    protected final TargetType<D> destinationTargetType;
    protected final Function<S, D> targetMapper;
    protected final BiConsumer<C, E> innerConsumer;
    protected final BiConsumer<S, E> outerConsumer;
    protected final int cIdx, tIdxF, tIdxL;

    public EventListenerForwardingByLambda(@NotNull EventListener<E, DL, C> innerListener,
                                           @NotNull TargetType<S> sourceTargetType,
                                           @NotNull TargetType<D> destinationTargetType,
                                           @NotNull Function<S, D> targetMapper) {

        super(Objects.requireNonNull(innerListener).getEventType(),
                sourceTargetType.getIdentityComponentType(), innerListener.getPriority());

        this.innerListener = innerListener;
        this.innerConsumer = innerListener.getConsumer();
        this.cIdx = innerListener.getComponentType().getInternalIdx();
        this.tIdxF = innerListener.getComponentType().getTargetType().getSubtargetIdxFirst();
        this.tIdxL = innerListener.getComponentType().getTargetType().getSubtargetIdxLast();
        this.destinationTargetType = Objects.requireNonNull(destinationTargetType);
        this.targetMapper = Objects.requireNonNull(targetMapper);

        if (destinationTargetType.isEquivalentTo(innerListener.getComponentType().getTargetType())) {
            this.outerConsumer = this::invokeExact;
        } else if (innerListener.getComponentType().getTargetType().isEquivalentTo(destinationTargetType)) {
            this.outerConsumer = this::invoke;
        } else {
            throw FormatUtil.rtExc("Broken event forwarding, invalid listener []", this);
        }
    }

    @Override
    public BiConsumer<S, E> getConsumer() {
        return outerConsumer;
    }

    private void invokeExact(S target, E event) {
        final var innerTarget = targetMapper.apply(target);
        if (innerTarget == null) return;
        final var container = innerTarget.getComponentContainer();
        @SuppressWarnings("unchecked")
        final C innerComponent = (C) container.getComponent(cIdx);
        if (innerComponent != null) {
            innerConsumer.accept(innerComponent, event);
        }
    }

    private void invoke(S target, E event) {
        final var innerTarget = targetMapper.apply(target);
        if (innerTarget == null) return;
        final var container = innerTarget.getComponentContainer();
        final var tIdx = container.getTargetType().getSubtargetIdxFirst();
        if (tIdx >= tIdxF && tIdx <= tIdxL) {
            @SuppressWarnings("unchecked")
            final C innerComponent = (C) container.getComponent(cIdx);
            if (innerComponent != null) {
                innerConsumer.accept(innerComponent, event);
            }
        }
    }

    public EventListener<E, DL, C> getInnerListener() {
        return innerListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventListenerForwardingByLambda)) return false;
        if (!super.equals(o)) return false;
        final var that = (EventListenerForwardingByLambda<?, ?, ?, ?, ?>) o;
        return innerListener.equals(that.innerListener) && targetMapper.equals(that.targetMapper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), innerListener);
    }

}
