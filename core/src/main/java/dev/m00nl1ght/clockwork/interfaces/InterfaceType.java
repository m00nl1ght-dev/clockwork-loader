package dev.m00nl1ght.clockwork.interfaces;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class InterfaceType<I, T extends ComponentTarget> {

    protected final Class<I> interfaceClass;
    protected final TargetType<T> targetType;

    protected InterfaceType(Class<I> interfaceClass, TargetType<T> targetType) {
        this.interfaceClass = Arguments.notNull(interfaceClass, "interfaceClass");
        this.targetType = Arguments.notNull(targetType, "targetType");
        targetType.requireInitialised();
    }

    @SuppressWarnings("unchecked")
    public void autoCollectComponents() {
        addComponents(targetType.getAllSubtargets().stream()
                .flatMap(subtarget -> subtarget.getComponentTypes().stream())
                .filter(comp -> comp.getParent() == null && interfaceClass.isAssignableFrom(comp.getComponentClass()))
                .map(comp -> (ComponentType<? extends I, ? extends T>) comp)
                .collect(Collectors.toList()));
    }

    public abstract void apply(T object, Consumer<? super I> consumer);

    public abstract Iterator<I> iterator(T object);

    public abstract Spliterator<I> spliterator(T object);

    public Stream<I> stream(T object) {
        return StreamSupport.stream(spliterator(object), false);
    }

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

    public final TargetType<T> getTargetType() {
        return targetType;
    }

    public Collection<TargetType<? extends T>> getCompatibleTargetTypes() {
        return targetType.getAllSubtargets();
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
