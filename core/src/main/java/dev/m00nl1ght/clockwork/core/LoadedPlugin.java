package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;
import dev.m00nl1ght.clockwork.version.Version;

import java.util.*;

public final class LoadedPlugin {

    private final PluginDescriptor descriptor;
    private final ClockworkCore clockworkCore;
    private final Set<ComponentType<?, ?>> componentTypes = new LinkedHashSet<>();
    private final Set<TargetType<?>> targetTypes = new LinkedHashSet<>();
    private final Module mainModule;

    LoadedPlugin(PluginDescriptor descriptor, ClockworkCore clockworkCore, Module mainModule) {
        this.descriptor = Objects.requireNonNull(descriptor);
        this.clockworkCore = Objects.requireNonNull(clockworkCore);
        this.mainModule = Objects.requireNonNull(mainModule);
    }

    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    public String getId() {
        return descriptor.getId();
    }

    public Version getVersion() {
        return descriptor.getVersion();
    }

    public ClockworkCore getClockworkCore() {
        return clockworkCore;
    }

    @SuppressWarnings("unchecked")
    public ComponentType<?, ClockworkCore> getMainComponent() {
        final var component = clockworkCore.getComponentTypeOrThrow(getId());
        if (component.getTargetType().getTargetClass() != ClockworkCore.class) {
            throw new RuntimeException("Main component invalid: " + component);
        } else {
            return (ComponentType<?, ClockworkCore>) component;
        }
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
