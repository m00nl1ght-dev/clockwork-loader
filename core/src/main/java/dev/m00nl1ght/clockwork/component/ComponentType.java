package dev.m00nl1ght.clockwork.component;

import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.util.Objects;

public class ComponentType<C, T extends ComponentTarget> {

    protected final Class<C> componentClass;
    protected final TargetType<T> targetType;
    protected final TargetType<? super T> rootTargetType;

    private int internalIdx = -1;

    ComponentFactory<T, C> factory = ComponentFactory.emptyFactory();

    public ComponentType(TargetType<T> targetType, Class<C> componentClass) {
        this.targetType = Objects.requireNonNull(targetType);
        this.componentClass = Objects.requireNonNull(componentClass);
        this.rootTargetType = targetType.getRoot();
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
        if (forType.getRoot() != rootTargetType) checkCompatibility(forType);
        return internalIdx;
    }

    @SuppressWarnings("unchecked")
    public C get(T object) {
        final var container = object.getComponentContainer();
        if (container.getTargetType().getRoot() != rootTargetType) checkCompatibility(container.getTargetType());
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

    public void setFactory(ComponentFactory<T, C> factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    public void checkValue(T target, C value) {
        // NO-OP
    }

    @Override
    public String toString() {
        return componentClass.getSimpleName() + "@" + targetType.toString();
    }

    protected void checkCompatibility(TargetType<?> otherTarget) {
        targetType.requireLocked();
        if (this.internalIdx < 0) throw new IllegalStateException();
        if (!otherTarget.isEquivalentTo(this.targetType)) {
            final var msg = "Cannot access component [] (registered to target []) from different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, "[]", this, targetType, otherTarget));
        }
    }

    protected final synchronized void setInternalIdx(int internalIdx) {
        if (internalIdx < 0) throw new IllegalArgumentException();
        if (this.internalIdx >= 0) throw new IllegalStateException();
        targetType.requireNotLocked();
        this.internalIdx = internalIdx;
    }

    public static class Identity<T extends ComponentTarget> extends ComponentType<T, T> {

        public Identity(TargetType<T> targetType) {
            super(targetType, targetType.getTargetClass());
            setFactory(t -> t);
        }

        @Override
        public void checkValue(T target, T value) {
            if (target != value) throw new RuntimeException("Invalid value: " + value);
        }

        @Override
        public String toString() {
            return "<Identity>@" + targetType.toString();
        }

    }

}
