package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.descriptor.TargetDescriptor;
import dev.m00nl1ght.clockwork.version.Version;

import java.util.Objects;

public final class RegisteredTargetType<T extends ComponentTarget> extends TargetType<T> {

    private final LoadedPlugin plugin;
    private final TargetDescriptor descriptor;

    RegisteredTargetType(LoadedPlugin plugin, TargetType<? super T> parent, TargetDescriptor descriptor, Class<T> targetClass) {
        super(parent, targetClass);
        this.descriptor = Objects.requireNonNull(descriptor);
        this.plugin = Objects.requireNonNull(plugin);
        if (!plugin.getId().equals(descriptor.getPluginId())) throw new IllegalArgumentException();
    }

    public LoadedPlugin getPlugin() {
        return plugin;
    }

    public ClockworkCore getClockworkCore() {
        return plugin.getClockworkCore();
    }

    public TargetDescriptor getDescriptor() {
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
        return getId();
    }

}
