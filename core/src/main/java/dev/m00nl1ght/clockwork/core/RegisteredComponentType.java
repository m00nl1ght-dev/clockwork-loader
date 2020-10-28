package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.descriptor.ComponentDescriptor;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.version.Version;

import java.util.Objects;

public final class RegisteredComponentType<C, T extends ComponentTarget> extends ComponentType<C, T> {

    private final LoadedPlugin plugin;
    private final ComponentDescriptor descriptor;

    RegisteredComponentType(LoadedPlugin plugin, ComponentType<? super C, ? super T> parent, ComponentDescriptor descriptor, Class<C> componentClass, RegisteredTargetType<T> targetType) {
        super(parent, componentClass, targetType);
        this.descriptor = Objects.requireNonNull(descriptor);
        this.plugin = Objects.requireNonNull(plugin);
        if (!plugin.getId().equals(descriptor.getPluginId())) throw new IllegalArgumentException();
        if (!targetType.getId().equals(descriptor.getTargetId())) throw new IllegalArgumentException();
        if (!componentClass.getName().equals(descriptor.getComponentClass())) throw new IllegalArgumentException();
        super.setFactory(ComponentFactory.buildDefaultFactory(ClockworkLoader.getInternalLookup(), componentClass, targetType.getTargetClass()));
    }

    @Override
    public ComponentFactory<T, C> getFactory() {
        if (!descriptor.isFactoryAccessEnabled())
            throw FormatUtil.illStateExc("Factory access is not enabled on component []", this);
        return super.getFactory();
    }

    @Override
    public void setFactory(ComponentFactory<T, C> factory) {
        if (!descriptor.isFactoryAccessEnabled())
            throw FormatUtil.illStateExc("Factory access is not enabled on component []", this);
        super.setFactory(factory);
    }

    ComponentFactory<T, C> getFactoryInternal() {
        return super.getFactory();
    }

    void setFactoryInternal(ComponentFactory<T, C> factory) {
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

    public Version getVersion() {
        return descriptor.getVersion();
    }

    @Override
    public String toString() {
        return getId() + "@" + targetType.toString();
    }

}
