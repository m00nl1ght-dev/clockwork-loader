package dev.m00nl1ght.clockwork.interfaces;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.*;

public abstract class AbstractComponentInterface<I, T extends ComponentTarget> implements ComponentInterface<I, T> {

    protected final TypeRef<I> interfaceType;
    protected final TargetType<T> targetType;

    protected final List[] components;
    protected final TargetType<? super T> rootTarget;
    protected final int idxOffset;

    protected AbstractComponentInterface(TypeRef<I> interfaceType, TargetType<T> targetType) {
        this.interfaceType = Objects.requireNonNull(interfaceType);
        this.targetType = Objects.requireNonNull(targetType);
        targetType.requireInitialised();
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
    public List<ComponentType<? extends I, ? extends T>> getEffectiveComponents(TargetType<? extends T> target) {
        if (target.getRoot() != rootTarget) checkCompatibility(target);
        try {
            final var list = new ArrayList<ComponentType<? extends I, ? extends T>>();
            TargetType<?> type = target;
            while (type != null) {
                @SuppressWarnings("unchecked")
                final List<ComponentType<? extends I, ? extends T>> got = components[type.getSubtargetIdxFirst() - idxOffset];
                if (got != null) list.addAll(got);
                if (type == this.targetType) break;
                final var castedType = type.getParent();
                type = castedType;
            }
            return list;
        } catch (Exception e) {
            checkCompatibility(target);
            throw e;
        }
    }

    @Override
    public <C> void addComponents(Iterable<ComponentType<? extends I, ? extends T>> components) {
        final var modified = new HashSet<TargetType<? extends T>>();
        for (final var component : Objects.requireNonNull(components)) {
            final var type = component.getTargetType();
            type.requireInitialised();
            if (type.getRoot() != rootTarget) checkCompatibility(type);
            final var idx = type.getSubtargetIdxFirst() - idxOffset;
            if (this.components[idx] == null) this.components[idx] = new ArrayList(5);
            @SuppressWarnings("unchecked")
            final List<ComponentType<? extends I, ? extends T>> list = this.components[idx];
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

    @Override
    public TypeRef<I> getInterfaceType() {
        return interfaceType;
    }

    @Override
    public TargetType<T> getTargetType() {
        return targetType;
    }

    @Override
    public Collection<TargetType<? extends T>> getCompatibleTargetTypes() {
        return targetType.getAllSubtargets();
    }

    protected void checkCompatibility(TargetType<?> otherType) {
        if (!otherType.isEquivalentTo(targetType)) {
            final var msg = "Cannot use interface type [] (created for target []) on different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, this, targetType, otherType));
        }
    }

    @Override
    public String toString() {
        return targetType == null ? interfaceType.getSimpleName() + "@?" : interfaceType.getSimpleName() + "@" + targetType;
    }

}
