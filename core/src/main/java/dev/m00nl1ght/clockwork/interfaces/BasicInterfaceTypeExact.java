package dev.m00nl1ght.clockwork.interfaces;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicInterfaceTypeExact<I, T extends ComponentTarget> extends InterfaceType<I, T> {

    protected List components;

    protected BasicInterfaceTypeExact(Class<I> interfaceClass, TargetType<T> targetType) {
        super(interfaceClass, targetType);
    }

    @SuppressWarnings("unchecked")
    public List<ComponentType<?, T>> getComponents() {
        return components;
    }

    @Override
    public <S extends T> List<ComponentType<? extends I, S>> getComponents(TargetType<S> target) {
        if (target != getTargetType()) checkCompatibility(target);
        try {
            @SuppressWarnings("unchecked")
            final List<ComponentType<? extends I, S>> list = components;
            if (list == null) return List.of();
            return list;
        } catch (Exception e) {
            checkCompatibility(target);
            throw e;
        }
    }

    @Override
    public <C> void addComponents(Iterable<ComponentType<? extends I, ? extends T>> components) {
        boolean modified = false;
        for (final var component : Arguments.notNull(components, "components")) {
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

    @Override
    protected void checkCompatibility(TargetType<?> otherType) {
        if (getTargetType() == null) {
            final var msg = "Interface type for [] is not registered";
            throw new IllegalArgumentException(FormatUtil.format(msg, interfaceClass.getSimpleName()));
        } else if (otherType != getTargetType()) {
            final var msg = "Cannot use interface type [] (created for target []) on different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, this, getTargetType(), otherType));
        }
    }

    protected abstract void onComponentsChanged();

}
