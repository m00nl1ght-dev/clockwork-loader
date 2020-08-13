package dev.m00nl1ght.clockwork.interfaces;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;

import java.util.List;
import java.util.function.Consumer;

public abstract class ComponentInterfaceType<I, T extends ComponentTarget> {

    protected final Class<I> interfaceClass;
    protected final Class<T> targetClass;

    private TargetType<T> targetType;

    protected ComponentInterfaceType(Class<I> interfaceClass, Class<T> targetClass) {
        this.targetClass = targetClass;
        this.interfaceClass = interfaceClass;
    }

    public final synchronized void register(TargetType<T> targetType) {
        if (this.targetType == targetType) return;
        if (this.targetType != null) throw new IllegalStateException();
        targetType.registerInterfaceType(this);
        this.targetType = targetType;
        init();
    }

    protected abstract void init();

    public abstract void apply(T object, Consumer<? super I> consumer);

    public abstract <S extends T> List<ComponentType<? extends I, ? super S>> getComponents(TargetType<S> target);

    public abstract void addComponent(ComponentType<? extends I, ? extends T> componentType);

    public final Class<I> getInterfaceClass() {
        return interfaceClass;
    }

    public final Class<T> getTargetClass() {
        return targetClass;
    }

    public final TargetType<T> getTargetType() {
        return targetType;
    }

}
