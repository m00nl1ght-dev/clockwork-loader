package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.EventDispatcherProfilerGroup;
import dev.m00nl1ght.clockwork.debug.profiler.Profilable;
import dev.m00nl1ght.clockwork.event.impl.bus.EventDispatcherImpl;
import dev.m00nl1ght.clockwork.event.impl.bus.EventDispatcherImplExact;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public interface EventDispatcher<E extends Event, T extends ComponentTarget> extends Profilable<EventDispatcherProfilerGroup<E, ? extends T>> {

    static <E extends Event, T extends ComponentTarget> EventDispatcher<E, T> of(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType) {

        Objects.requireNonNull(targetType).requireInitialised();
        if (targetType.getDirectSubtargets().isEmpty()) {
            return new EventDispatcherImplExact<>(eventType, targetType);
        } else {
            return new EventDispatcherImpl<>(eventType, targetType);
        }
    }

    static <E extends Event, T extends ComponentTarget> EventDispatcher<E, T> of(
            @NotNull Class<E> eventClass,
            @NotNull TargetType<T> targetType) {

        return of(TypeRef.of(eventClass), targetType);
    }

    @NotNull TypeRef<E> getEventType();

    @NotNull TargetType<T> getTargetType();

    @NotNull E post(@NotNull T object, @NotNull E event);

    <S extends T> @NotNull List<@NotNull EventListener<E, ? super S, ?>> getListeners(@NotNull TargetType<S> target);

    <S extends T> @Nullable EventListenerCollection<E, S> getListenerCollection(@NotNull TargetType<S> target);

    <S extends T> void setListenerCollection(@NotNull EventListenerCollection<E, S> collection);

    @NotNull Collection<TargetType<? extends T>> getCompatibleTargetTypes();

}
