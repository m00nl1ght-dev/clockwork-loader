package dev.m00nl1ght.clockwork.interfaces.impl;

import dev.m00nl1ght.clockwork.core.ComponentContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Modified version of java.util.Spliterators.ArraySpliterator.
 */
public class InterfaceSpliterator<T> implements Spliterator<T> {

    private final ComponentContainer container;
    private final int[] componentIdxs;
    private int index;
    private final int fence;
    private final int characteristics;

    public InterfaceSpliterator(@NotNull ComponentContainer container, int[] componentIdxs) {
        this(container, componentIdxs, 0, componentIdxs.length, Spliterator.DISTINCT | Spliterator.NONNULL);
    }

    public InterfaceSpliterator(@NotNull ComponentContainer container, int[] componentIdxs, int characteristics) {
        this(container, componentIdxs, 0, componentIdxs.length, characteristics);
    }

    public InterfaceSpliterator(@NotNull ComponentContainer container, int[] componentIdxs, int origin, int fence, int characteristics) {
        this.container = container;
        this.componentIdxs = componentIdxs;
        this.index = origin;
        this.fence = fence;
        this.characteristics = characteristics;
    }

    @Override
    public @Nullable Spliterator<T> trySplit() {
        int lo = index, mid = (lo + fence) >>> 1;
        return (lo >= mid) ? null : new InterfaceSpliterator<>(container, componentIdxs, lo, index = mid, characteristics);
    }

    @Override
    public void forEachRemaining(@NotNull Consumer<? super T> action) {
        ComponentContainer o = container; int[] a; int i, hi;
        if (action == null) throw new NullPointerException();
        if ((a = componentIdxs).length >= (hi = fence) && (i = index) >= 0 && i < (index = hi)) {
            do {
                @SuppressWarnings("unchecked")
                final var comp = (T) o.getComponent(a[i]);
                if (comp != null) action.accept(comp);
            } while (++i < hi);
        }
    }

    @Override
    public boolean tryAdvance(@NotNull Consumer<? super T> action) {
        if (action == null) throw new NullPointerException();
        while (index >= 0 && index < fence) {
            @SuppressWarnings("unchecked")
            final var comp = (T) container.getComponent(componentIdxs[index++]);
            if (comp == null) continue;
            action.accept(comp);
            return true;
        }
        return false;
    }

    @Override
    public long estimateSize() {
        return fence - index;
    }

    @Override
    public int characteristics() {
        return characteristics;
    }

}
