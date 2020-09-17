package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.descriptor.ComponentDescriptor;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;

public final class RegisteredComponentType<C, T extends ComponentTarget> extends ComponentType<C, T> {

    private final LoadedPlugin plugin;
    private final ComponentDescriptor descriptor;

    RegisteredComponentType(RegisteredComponentType<? super C, ? super T> parent, LoadedPlugin plugin, ComponentDescriptor descriptor, Class<C> componentClass, RegisteredTargetType<T> targetType) {
        super(parent, componentClass, targetType);
        this.descriptor = Arguments.notNull(descriptor, "descriptor");
        this.plugin = Arguments.notNullAnd(plugin, o -> o.getId().equals(descriptor.getPlugin().getId()), "plugin");
        Arguments.notNullAnd(targetType, o -> o.getId().equals(descriptor.getTargetId()), "targetType");
        Arguments.notNullAnd(componentClass, o -> o.getName().equals(descriptor.getComponentClass()), "componentClass");
        super.setFactory(ComponentFactory.buildDefaultFactory(ClockworkLoader.getInternalReflectiveAccess(), componentClass, targetType.getTargetClass()));
    }

    @Override
    public ComponentFactory<T, C> getFactory() {
        if (!descriptor.isFactoryAccessEnabled() && targetType.isInitialised())
            throw FormatUtil.illStateExc("Factory access is not enabled on component []", this);
        return super.getFactory();
    }

    @Override
    public void setFactory(ComponentFactory<T, C> factory) {
        if (!descriptor.isFactoryAccessEnabled() && targetType.isInitialised())
            throw FormatUtil.illStateExc("Factory access is not enabled on component []", this);
        super.setFactory(factory);
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

    @Override
    public String toString() {
        return getId();
    }

}
