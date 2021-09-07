package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.component.ComponentFactory;
import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.ComponentType;
import dev.m00nl1ght.clockwork.descriptor.ComponentDescriptor;
import dev.m00nl1ght.clockwork.utils.logger.FormatUtil;
import dev.m00nl1ght.clockwork.utils.version.Version;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

public final class RegisteredComponentType<C extends Component<T>, T extends ComponentTarget> extends ComponentType<C, T> {

    private final LoadedPlugin plugin;
    private final ComponentDescriptor descriptor;

    RegisteredComponentType(LoadedPlugin plugin, ComponentDescriptor descriptor, RegisteredTargetType<T> targetType, Class<C> componentClass) {
        super(targetType, componentClass);
        this.descriptor = Objects.requireNonNull(descriptor);
        this.plugin = Objects.requireNonNull(plugin);
        if (!plugin.getId().equals(descriptor.getPluginId())) throw new IllegalArgumentException();
        if (!targetType.getId().equals(descriptor.getTargetId())) throw new IllegalArgumentException();
        if (!componentClass.getName().equals(descriptor.getComponentClass())) throw new IllegalArgumentException();
    }

    @Override
    public void checkValue(T target, C value) {
        final var actualClass = value.getClass();
        if (actualClass != componentClass) {
            throw FormatUtil.rtExc("Unexpected component value of class [] in [], expected class []", actualClass, getId(), componentClass);
        }
    }

    @Override
    public void setFactory(ComponentFactory<T, C> factory) {
        if (!descriptor.factoryChangesAllowed())
            throw FormatUtil.illStateExc("Factory changes are not enabled on component []", this);
        super.setFactory(factory);
    }

    void setFactoryInternal(ComponentFactory<T, C> factory) {
        super.setFactory(factory);
    }

    boolean createDefaultFactory(MethodHandles.Lookup lookup) {
        final var defaultFactory = ComponentFactory.buildDefaultFactory(lookup, targetType.getTargetClass(), componentClass);
        if (defaultFactory == null) return false;
        super.setFactory(defaultFactory);
        return true;
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
