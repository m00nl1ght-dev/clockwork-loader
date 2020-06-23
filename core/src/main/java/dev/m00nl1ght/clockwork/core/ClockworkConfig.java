package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.locator.PluginLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClockworkConfig {

    private final List<PluginLocator> pluginLocators = new ArrayList<>();
    private final List<ComponentDescriptor> componentDescriptors = new ArrayList<>();

    public static Builder builder() {
        return new Builder();
    }

    public ClockworkConfig(Builder builder) {
        this.pluginLocators.addAll(builder.pluginLocators);
        this.componentDescriptors.addAll(builder.componentDescriptors);
    }

    public List<ComponentDescriptor> getComponentDescriptors() {
        return Collections.unmodifiableList(componentDescriptors);
    }

    public List<PluginLocator> getPluginLocators() {
        return Collections.unmodifiableList(pluginLocators);
    }

    public static class Builder {

        private final List<PluginLocator> pluginLocators = new ArrayList<>();
        private final List<ComponentDescriptor> componentDescriptors = new ArrayList<>();

        private Builder() {}

        public ClockworkConfig build() {
            return new ClockworkConfig(this);
        }

        public void addPluginLocator(PluginLocator locator) {
            if (pluginLocators.stream().anyMatch(d -> d.getName().equals(locator.getName())))
                throw new IllegalArgumentException("duplicate locator: " + locator.getName());
            this.pluginLocators.add(locator);
        }

        public void addComponentDescriptor(ComponentDescriptor descriptor) {
            if (componentDescriptors.stream().anyMatch(d -> d.getTarget().equals(descriptor.getTarget())))
                throw new IllegalArgumentException("duplicate component descriptor: " + descriptor.getTarget());
            this.componentDescriptors.add(descriptor);
        }

    }

}
