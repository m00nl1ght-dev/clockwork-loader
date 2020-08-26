package dev.m00nl1ght.clockwork.interfaces;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class ComponentInterfaceType<I, T extends ComponentTarget> {

    protected final Class<I> interfaceClass;
    protected final Class<T> targetClass;

    private TargetType<T> targetType;

    protected ComponentInterfaceType(Class<I> interfaceClass, Class<T> targetClass) {
        this.targetClass = targetClass;
        this.interfaceClass = interfaceClass;
    }

    protected ComponentInterfaceType(Class<I> interfaceClass, TargetType<T> targetType, boolean autoCollect) {
        this(interfaceClass, targetType.getTargetClass());
        this.register(targetType, autoCollect);
    }

    public final synchronized void register(TargetType<T> targetType, boolean autoCollect) {
        if (this.targetType != null) throw new IllegalStateException();
        targetType.getPlugin().getClockworkCore().getState().requireOrAfter(ClockworkCore.State.POPULATED);
        this.targetType = targetType;
        init();
        if (autoCollect) autoCollectComponents();
    }

    @SuppressWarnings("unchecked")
    private void autoCollectComponents() {
        addComponents(targetType.getAllSubtargets().stream()
                .flatMap(subtarget -> subtarget.getOwnComponentTypes().stream())
                .filter(comp -> interfaceClass.isAssignableFrom(comp.getComponentClass()))
                .map(comp -> (ComponentType<? extends I, ? extends T>) comp)
                .collect(Collectors.toList()));
    }

    protected abstract void init();

    public abstract void apply(T object, Consumer<? super I> consumer);

    public abstract <S extends T> List<ComponentType<? extends I, ? super S>> getComponents(TargetType<S> target);

    public <C> void addComponent(ComponentType<? extends I, ? extends T> component) {
        this.addComponents(List.of(component));
    }

    public abstract <C> void addComponents(Iterable<ComponentType<? extends I, ? extends T>> components);

    public <C> void removeComponent(ComponentType<? extends I, ? extends T> component) {
        this.removeComponents(List.of(component));
    }

    public abstract <C> void removeComponents(Iterable<ComponentType<? extends I, ? extends T>> components);

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
