package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TargetType<T extends ComponentTarget> {

    protected final Class<T> targetClass;
    protected final TargetType<? super T> root;
    protected final TargetType<? super T> parent;

    private final List<TargetType<? extends T>> directSubtargets = new LinkedList<>();
    private final List<TargetType<?>> allSubtargets;

    private List<ComponentType<?, ? super T>> componentTypes;
    private int subtargetIdxFirst = -1, subtargetIdxLast = -1;

    public TargetType(TargetType<? super T> parent, Class<T> targetClass) {
        this.targetClass = Arguments.notNull(targetClass, "targetClass");
        this.parent = parent;
        if (this.parent == null) {
            this.root = this;
            this.allSubtargets = new LinkedList<>();
        } else {
            synchronized (this.parent) {
                this.root = this.parent.root;
                if (this.parent.isInitialised())
                    throw FormatUtil.illStateExc("Parent TargetType [] is already initialised", this.parent);
                this.parent.directSubtargets.add(this);
                this.allSubtargets = this.parent.allSubtargets;
            }
        }
    }

    public final Class<T> getTargetClass() {
        return targetClass;
    }

    public final TargetType<? super T> getParent() {
        return parent;
    }

    public final TargetType<? super T> getRoot() {
        return root;
    }

    public boolean isEquivalentTo(TargetType<?> other) {
        return other == this || (getParent() != null && getParent().isEquivalentTo(other));
    }

    public final List<ComponentType<?, ? super T>> getComponentTypes() {
        return componentTypes;
    }

    @SuppressWarnings("unchecked")
    public final ComponentType<T, T> getIdentityComponentType() {
        if (componentTypes == null) return null;
        return (ComponentType<T, T>) componentTypes.get(0);
    }

    @SuppressWarnings("unchecked")
    public final List<TargetType<? extends T>> getAllSubtargets() {
        requireInitialised();
        return IntStream.rangeClosed(subtargetIdxFirst, subtargetIdxLast)
                .mapToObj(i -> (TargetType<? extends T>) allSubtargets.get(i))
                .collect(Collectors.toUnmodifiableList());
    }

    public final List<TargetType<? extends T>> getDirectSubtargets() {
        requireInitialised();
        return Collections.unmodifiableList(directSubtargets);
    }

    public final int getSubtargetIdxFirst() {
        return subtargetIdxFirst;
    }

    public final int getSubtargetIdxLast() {
        return subtargetIdxLast;
    }

    public final boolean isInitialised() {
        return componentTypes != null;
    }

    public final void requireInitialised() {
        if (componentTypes == null) throw FormatUtil.illStateExc("TargetType [] is not initialised", this);
    }

    public final void requireNotInitialised() {
        if (componentTypes != null) throw FormatUtil.illStateExc("TargetType [] is initialised", this);
    }

    // ### Internal ###

    protected final synchronized void init(List<? extends ComponentType<?, ?>> components) {
        if (this.componentTypes != null) throw FormatUtil.illStateExc("TargetType [] is already initialised", this);
        components = Arguments.verifiedListSnapshot(components, t -> t.targetType == this, "components");
        final var componentList = new LinkedList<ComponentType<?, ? super T>>();

        if (parent == null) {
            this.populateSubtargets();
            componentList.add(new IdentityComponentType<>(this));
        } else {
            if (parent.componentTypes == null) throw FormatUtil.illStateExc("Parent TargetType [] is not initialised", parent);
            componentList.addAll(parent.componentTypes);
            componentList.set(0, new IdentityComponentType<>(this));
        }

        for (final var component : components) {
            @SuppressWarnings("unchecked")
            final var castedComponent = (ComponentType<?, T>) component;
            if (component.parent != null) {
                componentList.set(component.parent.getInternalIdx(), castedComponent);
            } else {
                componentList.add(castedComponent);
            }
        }

        for (int i = 0; i < componentList.size(); i++) {
            final var component = componentList.get(i);
            final var idx = component.getInternalIdx();
            if (idx < 0) {
                component.setInternalIdx(i);
            } else {
                if (idx != i) throw new IllegalStateException();
            }
        }

        this.componentTypes = List.copyOf(componentList);
    }

    private void populateSubtargets() {
        this.subtargetIdxFirst = this.allSubtargets.size();
        this.allSubtargets.add(this);
        for (final var sub : this.directSubtargets) sub.populateSubtargets();
        this.subtargetIdxLast = this.allSubtargets.size() - 1;
    }

}
