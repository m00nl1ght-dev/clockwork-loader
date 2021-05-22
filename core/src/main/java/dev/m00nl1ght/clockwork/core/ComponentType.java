package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.util.Objects;

public class ComponentType<C, T extends ComponentTarget> {

    protected final Class<C> componentClass;
    protected final TargetType<T> targetType;
    protected final TargetType<? super T> rootType;

    private int internalIdx = -1;

    ComponentFactory<T, C> factory = ComponentFactory.emptyFactory();

    public ComponentType(TargetType<T> targetType, Class<C> componentClass) {
        this.targetType = Objects.requireNonNull(targetType);
        this.componentClass = Objects.requireNonNull(componentClass);
        this.rootType = targetType.getRoot();
        this.targetType.registerComponentType(this);
    }

    public final TargetType<T> getTargetType() {
        return targetType;
    }

    public final Class<C> getComponentClass() {
        return componentClass;
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
        final var container = object.getComponentContainer();
        if (container.getTargetType().getRoot() != rootType) checkCompatibility(container.getTargetType());
        try {
            return (C) container.getComponent(internalIdx);
        } catch (Exception e) {
            checkCompatibility(container.getTargetType());
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
    }

}
