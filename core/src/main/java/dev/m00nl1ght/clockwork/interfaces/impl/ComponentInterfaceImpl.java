package dev.m00nl1ght.clockwork.interfaces.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.interfaces.ComponentInterface;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ComponentInterfaceImpl<I, T extends ComponentTarget> implements ComponentInterface<I, T> {

    protected final TypeRef<I> interfaceType;
    protected final TargetType<T> targetType;
    protected final int idxOffset;

    protected final int[][] compIds;

    public ComponentInterfaceImpl(@NotNull TypeRef<I> interfaceType, @NotNull TargetType<T> targetType) {
        this.interfaceType = Objects.requireNonNull(interfaceType);
        this.targetType = Objects.requireNonNull(targetType);
        targetType.requireInitialised();
        this.idxOffset = getTargetType().getSubtargetIdxFirst();
        final var cnt = getTargetType().getSubtargetIdxLast() - idxOffset + 1;
        this.compIds = new int[cnt][];
        for (final var target : targetType.getAllSubtargets()) {
            final var idx = target.getSubtargetIdxFirst() - idxOffset;
            compIds[idx] = ComponentInterface.buildIdxArray(interfaceType, target);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <S extends T> List<ComponentType<? extends I, ? super S>> getComponents(@NotNull TargetType<S> target) {
        checkCompatibility(target);
        final var comps = compIds[target.getSubtargetIdxFirst() - idxOffset];
        final var list = target.getComponentTypes();
        return Arrays.stream(comps)
                .mapToObj(i -> (ComponentType<? extends I, ? super S>) list.get(i))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void apply(@NotNull T object, @NotNull Consumer<? super I> consumer) {
        final var target = object.getTargetType();
        try {
            final var comps = compIds[target.getSubtargetIdxFirst() - idxOffset];
            for (final var idx : comps) {
                @SuppressWarnings("unchecked")
                final var comp = (I) object.getComponent(idx);
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
        final var target = object.getTargetType();
        try {
            final var comps = compIds[target.getSubtargetIdxFirst() - idxOffset];
            return new InterfaceIterator<>(object, comps);
        } catch (Throwable t) {
            checkCompatibility(target);
            throw t;
        }
    }

    @Override
    public @NotNull Spliterator<I> spliterator(@NotNull T object) {
        final var target = object.getTargetType();
        try {
            final var comps = compIds[target.getSubtargetIdxFirst() - idxOffset];
            return new InterfaceSpliterator<>(object, comps);
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
        return targetType.getAllSubtargets();
    }

    protected void checkCompatibility(@NotNull TargetType<?> otherTarget) {
        if (!otherTarget.isEquivalentTo(targetType)) {
            final var msg = "Target " + otherTarget + " is not compatible with component interface " + this;
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public String toString() {
        return interfaceType.getSimpleName() + "@" + targetType;
    }

}
