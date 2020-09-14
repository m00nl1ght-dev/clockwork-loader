package dev.m00nl1ght.clockwork.interfaces;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class BasicInterfaceType<I, T extends ComponentTarget> extends InterfaceType<I, T> {

    protected List[] components;
    protected TargetType<? super T> rootTarget;
    protected int idxOffset;

    protected BasicInterfaceType(Class<I> interfaceClass, Class<T> targetClass) {
        super(interfaceClass, targetClass);
    }

    protected BasicInterfaceType(Class<I> interfaceClass, TargetType<T> targetType, boolean autoCollect) {
        super(interfaceClass, targetType, autoCollect);
    }

    @Override
    protected void init() {
        this.rootTarget = getTargetType().getRoot();
        this.idxOffset = getTargetType().getSubtargetIdxFirst();
        final var cnt = getTargetType().getSubtargetIdxLast() - idxOffset + 1;
        this.components = new List[cnt];
    }

    @Override
    public <S extends T> List<ComponentType<? extends I, S>> getComponents(TargetType<S> target) {
        if (target.getRoot() != rootTarget) checkCompatibility(target);
        try {
            @SuppressWarnings("unchecked")
            final List<ComponentType<? extends I, S>> list = components[target.getSubtargetIdxFirst() - idxOffset];
            if (list == null) return List.of();
            return list;
        } catch (Exception e) {
            checkCompatibility(target);
            throw e;
        }
    }

    @Override
    public <C> void addComponents(Iterable<ComponentType<? extends I, ? extends T>> components) {
        final var modified = new HashSet<TargetType<? extends T>>();
        for (final var component : components) {
            final var type = component.getTargetType();
            if (type.getRoot() != rootTarget) checkCompatibility(type);
            final var idx = type.getSubtargetIdxFirst() - idxOffset;
            if (this.components[idx] == null) this.components[idx] = new ArrayList(5);
            @SuppressWarnings("unchecked") final List<ComponentType<? extends I, ? extends T>> list = this.components[idx];
            if (!list.contains(component)) {
                list.add(component);
                modified.addAll(type.getAllSubtargets());
            }
        }
        for (final var type : modified) {
            onComponentsChanged(type);
        }
    }

    @Override
    public <C> void removeComponents(Iterable<ComponentType<? extends I, ? extends T>> components) {
        final var modified = new HashSet<TargetType<? extends T>>();
        for (final var component : components) {
            final var type = component.getTargetType();
            if (type.getRoot() != rootTarget) checkCompatibility(type);
            final var idx = type.getSubtargetIdxFirst() - idxOffset;
            if (this.components[idx] == null) continue;
            final var list = this.components[idx];
            if (list.remove(component)) {
                modified.addAll(type.getAllSubtargets());
            }
        }
        for (final var type : modified) {
            onComponentsChanged(type);
        }
    }

    protected abstract void onComponentsChanged(TargetType<? extends T> targetType);

}
