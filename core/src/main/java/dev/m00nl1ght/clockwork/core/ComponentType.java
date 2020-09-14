package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.descriptor.ComponentDescriptor;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.Arguments;

public final class ComponentType<C, T extends ComponentTarget> {

    private final LoadedPlugin plugin;
    private final ComponentDescriptor descriptor;
    private final Class<C> componentClass;
    private final TargetType<T> targetType;
    private final TargetType<? super T> rootType;

    private int internalIdx = -1;
    private ComponentFactory<T, C> factory;

    ComponentType(LoadedPlugin plugin, ComponentDescriptor descriptor, Class<C> componentClass, TargetType<T> targetType) {
        this.descriptor = Arguments.notNull(descriptor, "descriptor");
        this.plugin = Arguments.notNullAnd(plugin, o -> o.getId().equals(descriptor.getPlugin().getId()), "plugin");
        this.targetType = Arguments.notNullAnd(targetType, o -> o.getId().equals(descriptor.getTargetId()), "targetType");
        this.componentClass = Arguments.notNullAnd(componentClass, o -> o.getName().equals(descriptor.getComponentClass()), "componentClass");
        this.factory = ComponentFactory.buildDefaultFactory(ClockworkLoader.getInternalReflectiveAccess(), componentClass, targetType.getTargetClass());
        this.rootType = targetType.getRoot();
    }

    public LoadedPlugin getPlugin() {
        return plugin;
    }

    public ClockworkCore getClockworkCore() {
        return plugin.getClockworkCore();
    }

    public ComponentDescriptor getDescriptor() {
        return descriptor;
    }

    public String getId() {
        return descriptor.getId();
    }

    public TargetType<T> getTargetType() {
        return targetType;
    }

    public Class<C> getComponentClass() {
        return componentClass;
    }

    @Override
    public String toString() {
        return descriptor.toString();
    }

    public int getInternalIdx() {
        getClockworkCore().getState().requireOrAfter(ClockworkCore.State.POPULATED);
        return internalIdx;
    }

    public int getInternalIdx(TargetType<?> forType) {
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
        if (!descriptor.isFactoryAccessEnabled()) throw new UnsupportedOperationException();
        return this.getFactoryInternal();
    }

    public void setFactory(ComponentFactory<T, C> factory) {
        if (!descriptor.isFactoryAccessEnabled()) throw new UnsupportedOperationException();
        this.setFactoryInternal(factory);
    }

    // ### Internal ###

    private void checkCompatibility(TargetType<?> otherTarget) {
        getClockworkCore().getState().requireOrAfter(ClockworkCore.State.POPULATED);
        if (!otherTarget.isEquivalentTo(this.targetType)) {
            final var msg = "Cannot access component [] (registered to target []) from different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, "[]", this, targetType, otherTarget));
        }
    }

    void setInternalIdx(int internalIdx) {
        getClockworkCore().getState().require(ClockworkCore.State.POPULATING);
        this.internalIdx = internalIdx;
    }

    ComponentFactory<T, C> getFactoryInternal() {
        return factory;
    }

    void setFactoryInternal(ComponentFactory<T, C> factory) {
        this.factory = factory;
    }

}
