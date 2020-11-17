package dev.m00nl1ght.clockwork.interfaces;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface ComponentInterface<I, T extends ComponentTarget> {

    // TODO rework concept and refactor impls

    void apply(T object, Consumer<? super I> consumer);

    Iterator<I> iterator(T object);

    Spliterator<I> spliterator(T object);

    default Stream<I> stream(T object) {
        return StreamSupport.stream(spliterator(object), false);
    }

    <S extends T> List<ComponentType<? extends I, S>> getComponents(TargetType<S> target);

    List<ComponentType<? extends I, ? extends T>> getEffectiveComponents(TargetType<? extends T> target);

    default <C> void addComponent(ComponentType<? extends I, ? extends T> component) {
        this.addComponents(List.of(component));
    }

    <C> void addComponents(Iterable<ComponentType<? extends I, ? extends T>> components);

    default <C> void removeComponent(ComponentType<? extends I, ? extends T> component) {
        this.removeComponents(List.of(component));
    }

    <C> void removeComponents(Iterable<ComponentType<? extends I, ? extends T>> components);

    TypeRef<I> getInterfaceType();

    TargetType<T> getTargetType();

    Collection<TargetType<? extends T>> getCompatibleTargetTypes();

    @SuppressWarnings("unchecked")
    default void autoCollectComponents() {
        addComponents(getTargetType().getAllSubtargets().stream()
                .flatMap(subtarget -> subtarget.getComponentTypes().stream())
                .filter(comp -> comp.getParent() == null && getInterfaceType().tryFindAssignable(comp.getComponentClass()))
                .map(comp -> (ComponentType<? extends I, ? extends T>) comp)
                .collect(Collectors.toList()));
    }

}
