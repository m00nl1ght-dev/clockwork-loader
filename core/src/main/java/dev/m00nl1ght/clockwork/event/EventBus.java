package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.EventBusProfilerGroup;
import dev.m00nl1ght.clockwork.debug.profiler.Profilable;
import dev.m00nl1ght.clockwork.event.impl.forwarding.EventForwardingPolicyByComponent;
import dev.m00nl1ght.clockwork.event.impl.forwarding.EventForwardingPolicyByLambda;
import dev.m00nl1ght.clockwork.event.impl.listener.EventListenerSimple;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface EventBus<B extends Event> extends Profilable<EventBusProfilerGroup> {

    <E extends B, T extends ComponentTarget>
    @NotNull EventDispatcher<E, T> getEventDispatcher(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType);

    default <E extends B, T extends ComponentTarget>
    @NotNull EventDispatcher<E, T> getEventDispatcher(
            @NotNull Class<E> eventClass,
            @NotNull TargetType<T> targetType) {

        return getEventDispatcher(TypeRef.of(eventClass), targetType);
    }

    <E extends B, T extends ComponentTarget>
    @NotNull EventListenerCollection<E, T> getListenerCollection(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType);

    default <E extends B, T extends ComponentTarget>
    @NotNull EventListenerCollection<E, T> getListenerCollection(
            @NotNull Class<E> eventClass,
            @NotNull TargetType<T> targetType) {

        return getListenerCollection(TypeRef.of(eventClass), targetType);
    }

    <S extends ComponentTarget, D extends ComponentTarget>
    boolean addForwardingPolicy(@NotNull EventForwardingPolicy<S, D> forwardingPolicy);

    <S extends ComponentTarget, D extends ComponentTarget>
    boolean removeForwardingPolicy(@NotNull EventForwardingPolicy<S, D> forwardingPolicy);

    @NotNull Set<@NotNull EventForwardingPolicy<?, ?>> getForwardingPolicies();

    default <S extends ComponentTarget, D extends ComponentTarget>
    void addForwardingPolicy(@NotNull TargetType<D> destination,
                             @NotNull ComponentType<D, S> linkingComponent) {

        addForwardingPolicy(new EventForwardingPolicyByComponent<>(destination, linkingComponent));
    }

    default <S extends ComponentTarget, D extends ComponentTarget>
    void addForwardingPolicy(@NotNull TargetType<S> source,
                             @NotNull TargetType<D> destination,
                             @NotNull Function<S, D> targetMapper) {

        addForwardingPolicy(new EventForwardingPolicyByLambda<>(source, destination, targetMapper));
    }

    default <E extends B, T extends ComponentTarget, C>
    boolean addListener(@NotNull EventListener<E, T, C> listener) {
        final var targetType = listener.getComponentType().getTargetType();
        return getListenerCollection(listener.getEventType(), targetType).add(listener);
    }

    default <E extends B, T extends ComponentTarget, C>
    @NotNull EventListener<E, T, C> addListener(
            @NotNull TypeRef<E> eventType,
            @NotNull ComponentType<C, T> componentType,
            @NotNull BiConsumer<C, E> consumer,
            @NotNull EventListener.Phase phase) {

        final var listener = new EventListenerSimple<>(eventType, componentType, phase, consumer);
        addListener(listener);
        return listener;
    }

    default <E extends B, T extends ComponentTarget, C>
    @NotNull EventListener<E, T, C> addListener(
            @NotNull TypeRef<E> eventType,
            @NotNull ComponentType<C, T> componentType,
            @NotNull BiConsumer<C, E> consumer) {

        return addListener(eventType, componentType, consumer, EventListener.Phase.NORMAL);
    }

    default <E extends B, T extends ComponentTarget, C>
    @NotNull EventListener<E, T, C> addListener(
            @NotNull Class<E> eventClass,
            @NotNull ComponentType<C, T> componentType,
            @NotNull BiConsumer<C, E> consumer,
            @NotNull EventListener.Phase phase) {

        return addListener(TypeRef.of(eventClass), componentType, consumer, phase);
    }

    default <E extends B, T extends ComponentTarget, C>
    @NotNull EventListener<E, T, C> addListener(
            @NotNull Class<E> eventClass,
            @NotNull ComponentType<C, T> componentType,
            @NotNull BiConsumer<C, E> consumer) {

        return addListener(TypeRef.of(eventClass), componentType, consumer, EventListener.Phase.NORMAL);
    }

    default <E extends B, T extends ComponentTarget, C>
    @NotNull EventListener<E, T, C> addListener(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer,
            @NotNull EventListener.Phase phase) {

        final var component = targetType.getOwnComponentTypeOrThrow(componentClass);
        return addListener(eventType, component, consumer, phase);
    }

    default <E extends B, T extends ComponentTarget, C>
    @NotNull EventListener<E, T, C> addListener(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer) {

        return addListener(eventType, targetType, componentClass, consumer, EventListener.Phase.NORMAL);
    }

    default <E extends B, T extends ComponentTarget, C>
    @NotNull EventListener<E, T, C> addListener(
            @NotNull Class<E> eventClass,
            @NotNull TargetType<T> targetType,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer,
            @NotNull EventListener.Phase phase) {

        return addListener(TypeRef.of(eventClass), targetType, componentClass, consumer, phase);
    }

    default <E extends B, T extends ComponentTarget, C>
    @NotNull EventListener<E, T, C> addListener(
            @NotNull Class<E> eventClass,
            @NotNull TargetType<T> targetType,
            @NotNull Class<C> componentClass,
            @NotNull BiConsumer<C, E> consumer) {

        return addListener(TypeRef.of(eventClass), targetType, componentClass, consumer, EventListener.Phase.NORMAL);
    }

}
