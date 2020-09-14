package dev.m00nl1ght.clockwork.interfaces;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.InterfaceProfilerGroup;
import dev.m00nl1ght.clockwork.util.Arguments;

import java.util.function.Consumer;

public class InterfaceTypeImplExact<I, T extends ComponentTarget> extends BasicInterfaceTypeExact<I, T> {

    private static final int[] EMPTY_ARRAY = new int[0];

    private int[] compIds = EMPTY_ARRAY;
    private InterfaceProfilerGroup<I, T> profilerGroup;
    private TargetType<T> exactType;

    public InterfaceTypeImplExact(Class<I> interfaceClass, Class<T> targetClass) {
        super(interfaceClass, targetClass);
    }

    public InterfaceTypeImplExact(Class<I> interfaceClass, TargetType<T> targetType, boolean autoCollect) {
        super(interfaceClass, targetType, autoCollect);
    }

    @Override
    protected void init() {
        this.exactType = getTargetType();
    }

    @Override
    protected void onComponentsChanged() {
        this.compIds = getComponents().stream().mapToInt(ComponentType::getInternalIdx).toArray();
    }

    @Override
    public void apply(T object, Consumer<? super I> consumer) {
        final var target = object.getTargetType();
        if (target != exactType) checkCompatibility(target);
        try {
            if (profilerGroup != null) {
                profilerGroup.begin(consumer);
                consumer = profilerGroup;
            }
            for (final var idx : compIds) {
                @SuppressWarnings("unchecked")
                final var comp = (I) object.getComponent(idx);
                try {
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

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void attachProfiler(InterfaceProfilerGroup<I, ? extends T> profilerGroup) {
        Arguments.notNull(profilerGroup, "profilerGroup");
        if (profilerGroup.getInterfaceType() != this) throw new IllegalArgumentException();
        checkCompatibility(profilerGroup.getTargetType());
        this.profilerGroup = (InterfaceProfilerGroup<I, T>) profilerGroup;
        onComponentsChanged();
    }

    @Override
    public synchronized void detachAllProfilers() {
        if (this.profilerGroup == null) return;
        this.profilerGroup = null;
        onComponentsChanged();
    }

    @Override
    public boolean supportsProfilers() {
        return true;
    }

}
