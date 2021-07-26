package dev.m00nl1ght.clockwork.interfaces.impl;

import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.ComponentType;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.component.TargetType;
import dev.m00nl1ght.clockwork.interfaces.ComponentInterface;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ComponentInterfaceImplExact<I, T extends ComponentTarget> implements ComponentInterface<I, T> {

    protected final TypeRef<I> interfaceType;
    protected final TargetType<T> targetType;

    protected final int[] compIds;

    public ComponentInterfaceImplExact(@NotNull TypeRef<I> interfaceType, @NotNull TargetType<T> targetType) {
        this.interfaceType = Objects.requireNonNull(interfaceType);
        this.targetType = Objects.requireNonNull(targetType);
        targetType.requireLocked();
        this.compIds = ComponentInterface.buildIdxArray(interfaceType, targetType);
    }

    public ComponentInterfaceImplExact(@NotNull Class<I> interfaceClass, @NotNull TargetType<T> targetType) {
        this(TypeRef.of(interfaceClass), targetType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <S extends T> List<ComponentType<? extends I, ? super S>> getComponents(@NotNull TargetType<S> target) {
        checkCompatibility(target);
        final var list = target.getComponentTypes();
        return Arrays.stream(compIds)
                .mapToObj(i -> (ComponentType<? extends I, ? super S>) list.get(i))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void apply(@NotNull T object, @NotNull Consumer<? super I> consumer) {
        final var container = object.getComponentContainer();
        final var target = container.getTargetType();
        if (target != targetType) checkCompatibility(target);
        try {
            for (final var idx : compIds) {
                @SuppressWarnings("unchecked")
                final var comp = (I) container.getComponent(idx);
                try {
                    if (comp != null) consumer.accept(comp);
                } catch (ExceptionInPlugin e) {
                    e.addComponentToStack(target.getComponentTypes().get(idx));
                    throw e;
                } catch (Throwable e) {
                    final var compType = target.getComponentTypes().get(idx);
                    throw ExceptionInPlugin.inComponentInterface(compType, interfaceType, e);
                }
            }
        } catch (Throwable t) {
            checkCompatibility(target);
            throw t;
        }
    }

    @Override
    public @NotNull Iterator<I> iterator(@NotNull T object) {
        final var container = object.getComponentContainer();
        final var target = container.getTargetType();
        if (target != targetType) checkCompatibility(target);
        try {
            return new InterfaceIterator<>(container, compIds);
        } catch (Throwable t) {
            checkCompatibility(target);
            throw t;
        }
    }

    @Override
    public @NotNull Spliterator<I> spliterator(@NotNull T object) {
        final var container = object.getComponentContainer();
        final var target = container.getTargetType();
        if (target != targetType) checkCompatibility(target);
        try {
            return new InterfaceSpliterator<>(container, compIds);
        } catch (Throwable t) {
            checkCompatibility(target);
            throw t;
        }
    }

    @Override
    public @NotNull TypeRef<I> getInterfaceType() {
        return interfaceType;
    }

    @Override
    public @NotNull TargetType<T> getTargetType() {
        return targetType;
    }

    @Override
    public @NotNull Collection<@NotNull TargetType<? extends T>> getCompatibleTargetTypes() {
        return List.of(targetType);
    }

    protected void checkCompatibility(@NotNull TargetType<?> otherTarget) {
        if (otherTarget != getTargetType()) {
            final var msg = "Target " + otherTarget + " is not compatible with component interface " + this;
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public String toString() {
        return interfaceType.getSimpleName() + "@" + targetType;
    }

}
