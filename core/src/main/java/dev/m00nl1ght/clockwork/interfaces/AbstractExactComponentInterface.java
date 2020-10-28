package dev.m00nl1ght.clockwork.interfaces;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class AbstractExactComponentInterface<I, T extends ComponentTarget> implements ComponentInterface<I, T> {

    protected final TypeRef<I> interfaceType;
    protected final TargetType<T> targetType;

    protected List components;

    protected AbstractExactComponentInterface(TypeRef<I> interfaceType, TargetType<T> targetType) {
        this.interfaceType = Objects.requireNonNull(interfaceType);
        this.targetType = Objects.requireNonNull(targetType);
        targetType.requireInitialised();
    }

    @SuppressWarnings("unchecked")
    public List<ComponentType<? extends I, T>> getComponents() {
        return components;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends T> List<ComponentType<? extends I, S>> getComponents(TargetType<S> target) {
        return getRawComponents(target);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ComponentType<? extends I, ? extends T>> getEffectiveComponents(TargetType<? extends T> target) {
        return getRawComponents(target);
    }

    protected List getRawComponents(TargetType<?> target) {
        if (target != getTargetType()) checkCompatibility(target);
        if (components == null) return List.of();
        return components;
    }

    @Override
    public <C> void addComponents(Iterable<ComponentType<? extends I, ? extends T>> components) {
        boolean modified = false;
        for (final var component : Objects.requireNonNull(components)) {
            final var type = component.getTargetType();
            type.requireInitialised();
            if (type != getTargetType()) checkCompatibility(type);
            if (this.components == null) this.components = new ArrayList(5);
            @SuppressWarnings("unchecked")
            final List<ComponentType<? extends I, ? extends T>> list = this.components;
            if (!list.contains(component)) {
                list.add(component);
                modified = true;
            }
        }
        if (modified) onComponentsChanged();
    }

    @Override
    public <C> void removeComponents(Iterable<ComponentType<? extends I, ? extends T>> components) {
        boolean modified = false;
        for (final var component : components) {
            final var type = component.getTargetType();
            if (type != getTargetType()) checkCompatibility(type);
            if (this.components == null) continue;
            if (this.components.remove(component)) {
                modified = true;
            }
        }
        if (modified) onComponentsChanged();
    }

    protected void checkCompatibility(TargetType<?> otherType) {
        if (otherType != getTargetType()) {
            final var msg = "Cannot use interface type [] (created for target []) on different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, this, getTargetType(), otherType));
        }
    }

    protected abstract void onComponentsChanged();

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
        return List.of(targetType);
    }

    @Override
    public String toString() {
        return targetType == null ? interfaceType.getSimpleName() + "@?" : interfaceType.getSimpleName() + "@" + targetType;
    }

}
