package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.component.ComponentType;
import dev.m00nl1ght.clockwork.component.TargetType;
import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;
import dev.m00nl1ght.clockwork.utils.version.Version;

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
    public ComponentType<? extends MainComponent, ClockworkCore> getMainComponent() {
        final var component = clockworkCore.getComponentTypeOrThrow(getId());
        if (component.getTargetType().getTargetClass() != ClockworkCore.class) {
            throw new RuntimeException("Main component invalid: " + component);
        } else if (!MainComponent.class.isAssignableFrom(component.getComponentClass())) {
            throw new RuntimeException("Main component class does not inherit from MainComponent: " + component);
        } else {
            return (ComponentType<? extends MainComponent, ClockworkCore>) component;
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
        clockworkCore.getPhase().require(ClockworkCore.Phase.CONSTRUCTED);
        targetTypes.add(targetType);
    }

    void addLoadedComponentType(ComponentType<?, ?> componentType) {
        clockworkCore.getPhase().require(ClockworkCore.Phase.CONSTRUCTED);
        componentTypes.add(componentType);
    }

}
