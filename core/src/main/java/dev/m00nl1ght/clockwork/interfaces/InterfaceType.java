package dev.m00nl1ght.clockwork.interfaces;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.InterfaceProfilerGroup;
import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class InterfaceType<I, T extends ComponentTarget> {

    protected final Class<I> interfaceClass;
    protected final Class<T> targetClass;

    private TargetType<T> targetType;

    protected InterfaceType(Class<I> interfaceClass, Class<T> targetClass) {
        this.targetClass = targetClass;
        this.interfaceClass = interfaceClass;
    }

    protected InterfaceType(Class<I> interfaceClass, TargetType<T> targetType, boolean autoCollect) {
        this(interfaceClass, targetType.getTargetClass());
        this.register(targetType, autoCollect);
    }

    public final synchronized void register(TargetType<T> targetType, boolean autoCollect) {
        if (this.targetType != null) throw new IllegalStateException();
        targetType.getClockworkCore().getState().requireOrAfter(ClockworkCore.State.POPULATED);
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

    public abstract <S extends T> List<ComponentType<? extends I, S>> getComponents(TargetType<S> target);

    public List<ComponentType<? extends I, ? extends T>> getEffectiveComponents(TargetType<? extends T> target) {
        final var list = new ArrayList<ComponentType<? extends I, ? extends T>>();
        forTargetAndParents(target, t -> list.addAll(getComponents(t)));
        return list;
    }

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

    public void attachProfiler(InterfaceProfilerGroup<I, ? extends T> profilerGroup) {
        throw FormatUtil.unspExc("This InterfaceType implementation does not support profilers");
    }

    public void detachAllProfilers() {}

    public boolean supportsProfilers() {
        return false;
    }

    @Override
    public String toString() {
        return targetType == null ? interfaceClass.getSimpleName() + "@?" : interfaceClass.getSimpleName() + "@" + targetType;
    }

    // ### Internal ###

    protected void forTargetAndParents(TargetType<? extends T> origin, Consumer<TargetType<? extends T>> consumer) {
        TargetType<? extends T> type = origin;
        while (type != null) {
            consumer.accept(type);
            if (type == this.targetType) break;
            @SuppressWarnings("unchecked")
            final var castedType = (TargetType<? extends T>) type.getParent();
            type = castedType;
        }
    }

    protected void checkCompatibility(TargetType<?> otherType) {
        if (targetType == null) {
            final var msg = "Interface type for [] is not registered";
            throw new IllegalArgumentException(FormatUtil.format(msg, interfaceClass.getSimpleName()));
        } else if (!otherType.isEquivalentTo(targetType)) {
            final var msg = "Cannot use interface type [] (created for target []) on different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, this, targetType, otherType));
        }
    }

}
