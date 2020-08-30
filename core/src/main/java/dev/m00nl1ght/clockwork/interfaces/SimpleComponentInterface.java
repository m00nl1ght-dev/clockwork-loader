package dev.m00nl1ght.clockwork.interfaces;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.core.TargetType;

import java.util.Arrays;
import java.util.function.Consumer;

public class SimpleComponentInterface<I, T extends ComponentTarget> extends BasicComponentInterface<I, T> {

    private static final int[] EMPTY_ARRAY = new int[0];

    private int[][] compIds;

    public SimpleComponentInterface(Class<I> interfaceClass, Class<T> targetClass) {
        super(interfaceClass, targetClass);
    }

    public SimpleComponentInterface(Class<I> interfaceClass, TargetType<T> targetType, boolean autoCollect) {
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
    protected void onListenersChanged(TargetType<? extends T> targetType) {
        final var listeners = getEffectiveComponents(targetType);
        final var idx = targetType.getSubtargetIdxFirst() - idxOffset;
        this.compIds[idx] = listeners.stream().mapToInt(ComponentType::getInternalIdx).toArray();
    }

    @Override
    public void apply(T object, Consumer<? super I> consumer) {
        final var container = object.getComponentContainer();
        final var target = container.getTargetType();
        if (target.getRoot() != rootTarget) checkCompatibility(target);
        try {
            final var comps = compIds[target.getSubtargetIdxFirst() - idxOffset];
            for (final var idx : comps) {
                try {
                    @SuppressWarnings("unchecked") final var comp = (I) container.getComponent(idx);
                    if (comp != null) consumer.accept(comp);
                } catch (ExceptionInPlugin e) {
                    throw e;
                } catch (Throwable e) {
                    final var compType = target.getAllComponentTypes().get(idx);
                    throw ExceptionInPlugin.inComponentInterface(compType, interfaceClass, e);
                }
            }
        } catch (Throwable t) {
            checkCompatibility(target);
            throw t;
        }
    }

}
