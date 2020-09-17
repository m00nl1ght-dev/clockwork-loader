package dev.m00nl1ght.clockwork.interfaces;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.InterfaceProfilerGroup;
import dev.m00nl1ght.clockwork.util.Arguments;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class InterfaceTypeImpl<I, T extends ComponentTarget> extends BasicInterfaceType<I, T> {

    protected static final int[] EMPTY_ARRAY = new int[0];

    protected int[][] compIds;
    protected InterfaceProfilerGroup[] profilerGroups;

    public InterfaceTypeImpl(Class<I> interfaceClass, Class<T> targetClass) {
        super(interfaceClass, targetClass);
    }

    public InterfaceTypeImpl(Class<I> interfaceClass, TargetType<T> targetType, boolean autoCollect) {
        super(interfaceClass, targetType, autoCollect);
    }

    @Override
    protected void init() {
        super.init();
        final var cnt = getTargetType().getSubtargetIdxLast() - idxOffset + 1;
        this.compIds = new int[cnt][];
        Arrays.fill(compIds, EMPTY_ARRAY);
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
            if (profilerGroups != null) {
                @SuppressWarnings("unchecked")
                final var profilerGroup = (InterfaceProfilerGroup<I, T>)
                        profilerGroups[target.getSubtargetIdxFirst() - idxOffset];
                if (profilerGroup != null) {
                    profilerGroup.begin(consumer);
                    consumer = profilerGroup;
                }
            }
            for (final var idx : comps) {
                @SuppressWarnings("unchecked")
                final var comp = (I) object.getComponent(idx);
                try {
                    if (comp != null) consumer.accept(comp);
                } catch (ExceptionInPlugin e) {
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

    @Override
    public synchronized void attachProfiler(InterfaceProfilerGroup<I, ? extends T> profilerGroup) {
        Arguments.notNull(profilerGroup, "profilerGroup");
        if (this.profilerGroups == null) this.profilerGroups = new InterfaceProfilerGroup[compIds.length];
        if (profilerGroup.getInterfaceType() != this) throw new IllegalArgumentException();
        checkCompatibility(profilerGroup.getTargetType());
        this.profilerGroups[profilerGroup.getTargetType().getSubtargetIdxFirst() - idxOffset] = profilerGroup;
        onComponentsChanged(profilerGroup.getTargetType());
    }

    @Override
    public synchronized void detachAllProfilers() {
        if (this.profilerGroups == null) return;
        this.profilerGroups = null;
        for (final var type : getTargetType().getAllSubtargets()) {
            onComponentsChanged(type);
        }
    }

    @Override
    public boolean supportsProfilers() {
        return true;
    }

}
