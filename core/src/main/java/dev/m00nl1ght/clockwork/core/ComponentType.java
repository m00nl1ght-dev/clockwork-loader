package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.Arguments;

public class ComponentType<C, T extends ComponentTarget> {

    protected final Class<C> componentClass;
    protected final TargetType<T> targetType;
    protected final TargetType<? super T> rootType;
    protected final ComponentType<? super C, ? super T> parent;

    private int internalIdx = -1;
    ComponentFactory<T, C> factory = ComponentFactory.emptyFactory();

    public ComponentType(ComponentType<? super C, ? super T> parent, Class<C> componentClass, TargetType<T> targetType) {
        this.parent = Arguments.nullOr(parent, p -> targetType.isEquivalentTo(p.targetType) && targetType != p.targetType,"parent");
        this.targetType = Arguments.notNull(targetType, "targetType");
        this.componentClass = Arguments.notNull(componentClass, "componentClass");
        this.rootType = targetType.getRoot();
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

    public void setFactory(ComponentFactory<T, C> factory) {
        this.factory = Arguments.notNull(factory, "factory");
    }

    @Override
    public String toString() {
        return componentClass.getSimpleName() + "@" + targetType.toString();
    }

    // ### Internal ###

    protected void checkCompatibility(TargetType<?> otherTarget) {
        targetType.requireInitialised();
        if (!otherTarget.isEquivalentTo(this.targetType)) {
            final var msg = "Cannot access component [] (registered to target []) from different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, "[]", this, targetType, otherTarget));
        }
    }

    protected final void setInternalIdx(int internalIdx) {
        targetType.requireNotInitialised();
        this.internalIdx = internalIdx;
    }

}
