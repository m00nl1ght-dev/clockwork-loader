package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.descriptor.TargetDescriptor;
import dev.m00nl1ght.clockwork.util.Arguments;

public final class RegisteredTargetType<T extends ComponentTarget> extends TargetType<T> {

    private final LoadedPlugin plugin;
    private final TargetDescriptor descriptor;

    RegisteredTargetType(LoadedPlugin plugin, TargetType<? super T> parent, TargetDescriptor descriptor, Class<T> targetClass) {
        super(parent, targetClass);
        this.descriptor = Arguments.notNull(descriptor, "descriptor");
        this.plugin = Arguments.notNullAnd(plugin, o -> o.getId().equals(descriptor.getPlugin().getId()), "plugin");
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

    @Override
    public String toString() {
        return getId();
    }

}
