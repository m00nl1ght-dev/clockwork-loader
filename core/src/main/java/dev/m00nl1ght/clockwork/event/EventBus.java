package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.ComponentType;
import dev.m00nl1ght.clockwork.component.TargetType;
import dev.m00nl1ght.clockwork.event.debug.EventBusProfilerGroup;
import dev.m00nl1ght.clockwork.utils.debug.profiler.Profilable;
import dev.m00nl1ght.clockwork.event.impl.forwarding.EventForwardingPolicyByComponent;
import dev.m00nl1ght.clockwork.event.impl.forwarding.EventForwardingPolicyByLambda;
import dev.m00nl1ght.clockwork.event.impl.listener.EventListenerSimple;
import dev.m00nl1ght.clockwork.utils.reflect.TypeRef;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

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
    boolean addForwardingPolicy(@NotNull EventForwardingPolicy<B, S, D> forwardingPolicy);

    <S extends ComponentTarget, D extends ComponentTarget>
    boolean removeForwardingPolicy(@NotNull EventForwardingPolicy<B, S, D> forwardingPolicy);

    @NotNull Set<@NotNull EventForwardingPolicy<B, ?, ?>> getForwardingPolicies();

    default <S extends ComponentTarget, D extends ComponentTarget>
    void addForwardingPolicy(@NotNull TargetType<D> destination,
                             @NotNull ComponentType<D, S> linkingComponent,
                             @NotNull Predicate<TypeRef<? extends B>> eventTypeFilter) {

        addForwardingPolicy(new EventForwardingPolicyByComponent<>(
                destination, linkingComponent, eventTypeFilter, this));
    }

    default <S extends ComponentTarget, D extends ComponentTarget>
    void addForwardingPolicy(@NotNull TargetType<D> destination,
                             @NotNull ComponentType<D, S> linkingComponent) {

        addForwardingPolicy(destination, linkingComponent, e -> true);
    }

    default <S extends ComponentTarget, D extends ComponentTarget>
    void addForwardingPolicy(@NotNull TargetType<S> source,
                             @NotNull TargetType<D> destination,
                             @NotNull Predicate<TypeRef<? extends B>> eventTypeFilter,
                             @NotNull Function<S, D> targetMapper) {

        addForwardingPolicy(new EventForwardingPolicyByLambda<>(
                source, destination, eventTypeFilter, targetMapper, this));
    }

    default <S extends ComponentTarget, D extends ComponentTarget>
    void addForwardingPolicy(@NotNull TargetType<S> source,
                             @NotNull TargetType<D> destination,
                             @NotNull Function<S, D> targetMapper) {

        addForwardingPolicy(source, destination, e -> true, targetMapper);
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

    Class<B> getBaseEventClass();

}
