package dev.m00nl1ght.clockwork.interfaces;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.core.TargetType;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class InterfaceTypeImpl<I, T extends ComponentTarget> extends BasicInterfaceType<I, T> {

    protected static final int[] EMPTY_ARRAY = new int[0];

    protected final int[][] compIds;

    public InterfaceTypeImpl(Class<I> interfaceClass, TargetType<T> targetType) {
        super(interfaceClass, targetType);
        final var cnt = getTargetType().getSubtargetIdxLast() - idxOffset + 1;
        this.compIds = new int[cnt][];
        Arrays.fill(compIds, EMPTY_ARRAY);
    }

    public InterfaceTypeImpl(Class<I> interfaceClass, TargetType<T> targetType, boolean autoCollect) {
        this(interfaceClass, targetType);
        if (autoCollect) autoCollectComponents();
    }

    @Override
    protected void onComponentsChanged(TargetType<? extends T> targetType) {
        final var listeners = getEffectiveComponents(targetType);
        final var idx = targetType.getSubtargetIdxFirst() - idxOffset;
        this.compIds[idx] = listeners.stream().mapToInt(ComponentType::getInternalIdx).distinct().toArray();
    }

    @Override
    public void apply(T object, Consumer<? super I> consumer) {
        final var target = object.getTargetType();
        if (target.getRoot() != rootTarget) checkCompatibility(target);
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
                    throw ExceptionInPlugin.inComponentInterface(compType, interfaceClass, e);
                }
            }
        } catch (Throwable t) {
            checkCompatibility(target);
            throw t;
        }
    }

    @Override
    public Iterator<I> iterator(T object) {
        final var target = object.getTargetType();
        if (target.getRoot() != rootTarget) checkCompatibility(target);
        try {
            final var comps = compIds[target.getSubtargetIdxFirst() - idxOffset];
            return new InterfaceIterator<>(object, comps);
        } catch (Throwable t) {
            checkCompatibility(target);
            throw t;
        }
    }

    @Override
    public Spliterator<I> spliterator(T object) {
        final var target = object.getTargetType();
        if (target.getRoot() != rootTarget) checkCompatibility(target);
        try {
            final var comps = compIds[target.getSubtargetIdxFirst() - idxOffset];
            return new InterfaceSpliterator<>(object, comps);
        } catch (Throwable t) {
            checkCompatibility(target);
            throw t;
        }
    }

}
