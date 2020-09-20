package dev.m00nl1ght.clockwork.interfaces;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.core.TargetType;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class InterfaceTypeImplExact<I, T extends ComponentTarget> extends BasicInterfaceTypeExact<I, T> {

    protected static final int[] EMPTY_ARRAY = new int[0];

    protected int[] compIds = EMPTY_ARRAY;

    public InterfaceTypeImplExact(Class<I> interfaceClass, TargetType<T> targetType) {
        super(interfaceClass, targetType);
    }

    public InterfaceTypeImplExact(Class<I> interfaceClass, TargetType<T> targetType, boolean autoCollect) {
        this(interfaceClass, targetType);
        if (autoCollect) autoCollectComponents();
    }

    @Override
    protected void onComponentsChanged() {
        this.compIds = getComponents().stream().mapToInt(ComponentType::getInternalIdx).distinct().toArray();
    }

    @Override
    public void apply(T object, Consumer<? super I> consumer) {
        final var target = object.getTargetType();
        if (target != targetType) checkCompatibility(target);
        try {
            for (final var idx : compIds) {
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
        if (target != targetType) checkCompatibility(target);
        try {
            return new InterfaceIterator<>(object, compIds);
        } catch (Throwable t) {
            checkCompatibility(target);
            throw t;
        }
    }

    @Override
    public Spliterator<I> spliterator(T object) {
        final var target = object.getTargetType();
        if (target != targetType) checkCompatibility(target);
        try {
            return new InterfaceSpliterator<>(object, compIds);
        } catch (Throwable t) {
            checkCompatibility(target);
            throw t;
        }
    }

}
