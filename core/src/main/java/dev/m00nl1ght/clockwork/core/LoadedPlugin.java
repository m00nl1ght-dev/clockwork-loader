package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;
import dev.m00nl1ght.clockwork.util.Arguments;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class LoadedPlugin {

    private final PluginDescriptor descriptor;
    private final ClockworkCore clockworkCore;
    private final Set<ComponentType<?, ?>> componentTypes = new LinkedHashSet<>();
    private final Set<TargetType<?>> targetTypes = new LinkedHashSet<>();
    private final Module mainModule;

    LoadedPlugin(PluginDescriptor descriptor, ClockworkCore clockworkCore, Module mainModule) {
        this.descriptor = Arguments.notNull(descriptor, "descriptor");
        this.clockworkCore = Arguments.notNull(clockworkCore, "clockworkCore");
        this.mainModule = Arguments.notNull(mainModule, "mainModule");
    }

    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    public String getId() {
        return descriptor.getId();
    }

    public ClockworkCore getClockworkCore() {
        return clockworkCore;
    }

    public ComponentType<?, ClockworkCore> getMainComponent() {
        return clockworkCore.getComponentType(getId(), ClockworkCore.class).orElseThrow();
    }

    public Collection<ComponentType<?, ?>> getComponentTypes() {
        return Collections.unmodifiableCollection(componentTypes);
    }

    public Collection<TargetType<?>> getTargetTypes() {
        return Collections.unmodifiableCollection(targetTypes);
    }

    public Module getMainModule() {
        return mainModule;
    }

    @Override
    public String toString() {
        return descriptor.toString();
    }

    // ### Internal ###

    void addLoadedTargetType(TargetType<?> targetType) {
        clockworkCore.getState().require(ClockworkCore.State.POPULATING);
        targetTypes.add(targetType);
    }

    void addLoadedComponentType(ComponentType<?, ?> componentType) {
        clockworkCore.getState().require(ClockworkCore.State.POPULATING);
        componentTypes.add(componentType);
    }

}
