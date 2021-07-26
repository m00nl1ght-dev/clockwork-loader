package dev.m00nl1ght.clockwork.interfaces;

import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.ComponentType;
import dev.m00nl1ght.clockwork.component.TargetType;
import dev.m00nl1ght.clockwork.interfaces.impl.ComponentInterfaceImpl;
import dev.m00nl1ght.clockwork.interfaces.impl.ComponentInterfaceImplExact;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface ComponentInterface<I, T extends ComponentTarget> {

    static <I, T extends ComponentTarget> ComponentInterface<I, T> of(
            @NotNull TypeRef<I> interfaceType,
            @NotNull TargetType<T> targetType) {

        Objects.requireNonNull(targetType).requireLocked();
        if (targetType.getDirectSubtargets().isEmpty()) {
            return new ComponentInterfaceImplExact<>(interfaceType, targetType);
        } else {
            return new ComponentInterfaceImpl<>(interfaceType, targetType);
        }
    }

    static <I, T extends ComponentTarget> ComponentInterface<I, T> of(
            @NotNull Class<I> interfaceClass,
            @NotNull TargetType<T> targetType) {

        return of(TypeRef.of(interfaceClass), targetType);
    }

    @NotNull TypeRef<I> getInterfaceType();

    @NotNull TargetType<T> getTargetType();

    void apply(@NotNull T object, @NotNull Consumer<? super I> consumer);

    @NotNull Iterator<I> iterator(@NotNull T object);

    @NotNull Spliterator<I> spliterator(@NotNull T object);

    default @NotNull Stream<I> stream(@NotNull T object) {
        return StreamSupport.stream(spliterator(object), false);
    }

    <S extends T> @NotNull List<ComponentType<? extends I, ? super S>> getComponents(@NotNull TargetType<S> target);

    @NotNull Collection<TargetType<? extends T>> getCompatibleTargetTypes();

    static int[] buildIdxArray(@NotNull TypeRef<?> interfaceType, @NotNull TargetType<?> targetType) {
        return targetType.getComponentTypes().stream()
                .filter(comp -> interfaceType.tryFindAssignable(comp.getComponentClass()))
                .mapToInt(ComponentType::getInternalIdx).toArray();
    }

}
