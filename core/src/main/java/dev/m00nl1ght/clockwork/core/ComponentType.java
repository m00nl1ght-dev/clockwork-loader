package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ComponentType<C, T extends ComponentTarget> {

    protected final Class<C> componentClass;
    protected final TargetType<T> targetType;
    protected final TargetType<? super T> rootType;
    protected final ComponentType<? super C, ? super T> parent;

    private final List<ComponentType<? extends C, ? extends T>> directSubcomponents = new LinkedList<>();
    private int internalIdx = -1;

    ComponentFactory<T, C> factory = ComponentFactory.emptyFactory();

    public ComponentType(ComponentType<? super C, ? super T> parent, Class<C> componentClass, TargetType<T> targetType) {
        this.parent = parent;
        this.targetType = Objects.requireNonNull(targetType);
        this.componentClass = Objects.requireNonNull(componentClass);
        this.rootType = targetType.getRoot();
        this.targetType.registerComponentType(this);
        if (parent != null) {
            synchronized (this.parent) {
                if (!targetType.isEquivalentTo(parent.targetType) || targetType == parent.targetType)
                    throw FormatUtil.illStateExc("Parent ComponentType [] is incompatible", this.parent);
                if (!this.parent.componentClass.isAssignableFrom(componentClass))
                    throw FormatUtil.illArgExc("Heap pollution: Incompatible parent component [] for []", parent, this);
                if (this.parent.isInitialised())
                    throw FormatUtil.illStateExc("Parent ComponentType [] is already initialised", this.parent);
                this.parent.directSubcomponents.add(this);
            }
        }
    }

    public final TargetType<T> getTargetType() {
        return targetType;
    }

    public final Class<C> getComponentClass() {
        return componentClass;
    }

    public ComponentType<? super C, ? super T> getParent() {
        return parent;
    }

    public final List<ComponentType<? extends C, ? extends T>> getDirectSubcomponents() {
        this.requireInitialised();
        return Collections.unmodifiableList(directSubcomponents);
    }

    public final int getInternalIdx() {
        return internalIdx;
    }

    public final int getInternalIdx(TargetType<?> forType) {
        if (forType.getRoot() != rootType) checkCompatibility(forType);
        return internalIdx;
    }

    @SuppressWarnings("unchecked")
    public C get(T object) {
        if (object.getTargetType().getRoot() != rootType) checkCompatibility(object.getTargetType());
        try {
            return (C) object.getComponent(internalIdx);
        } catch (Exception e) {
            checkCompatibility(object.getTargetType());
            throw e;
        }
    }

    public ComponentFactory<T, C> getFactory() {
        return factory;
    }

    public void checkValue(T target, C value) {
        // NO-OP
    }

    public void setFactory(ComponentFactory<T, C> factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    public final boolean isInitialised() {
        return internalIdx >= 0;
    }

    public final void requireInitialised() {
        if (internalIdx < 0) throw FormatUtil.illStateExc("ComponentType [] is not initialised", this);
    }

    public final void requireNotInitialised() {
        if (internalIdx >= 0) throw FormatUtil.illStateExc("ComponentType [] is initialised", this);
    }

    @Override
    public String toString() {
        return componentClass.getSimpleName() + "@" + targetType.toString();
    }

    // ### Internal ###

    protected void checkCompatibility(TargetType<?> otherTarget) {
        this.requireInitialised();
        targetType.requireInitialised();
        if (!otherTarget.isEquivalentTo(this.targetType)) {
            final var msg = "Cannot access component [] (registered to target []) from different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, "[]", this, targetType, otherTarget));
        }
    }

    protected final synchronized void init(int internalIdx) {
        if (internalIdx < 0) throw new IllegalArgumentException();
        this.requireNotInitialised();
        targetType.requireNotInitialised();
        this.internalIdx = internalIdx;
        if (parent != null) verifySiblings();
    }

    private void verifySiblings() {
        for (final var sibling : parent.directSubcomponents) {
            if (sibling == this) continue;
            if (this.componentClass.isAssignableFrom(sibling.componentClass)) {
                throw PluginLoadingException.illegalSubcomponent(this, sibling);
            }
        }
    }

}
