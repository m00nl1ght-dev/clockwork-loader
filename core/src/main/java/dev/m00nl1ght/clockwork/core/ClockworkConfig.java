package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.locator.PluginLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClockworkConfig {

    private final List<PluginLocator> pluginLocators = new ArrayList<>();
    private final List<DependencyDescriptor> dependencyDescriptors = new ArrayList<>();

    public static Builder builder() {
        return new Builder();
    }

    public ClockworkConfig(Builder builder) {
        this.pluginLocators.addAll(builder.pluginLocators);
        this.dependencyDescriptors.addAll(builder.dependencyDescriptors);
    }

    public List<DependencyDescriptor> getComponentDescriptors() {
        return Collections.unmodifiableList(dependencyDescriptors);
    }

    public List<PluginLocator> getPluginLocators() {
        return Collections.unmodifiableList(pluginLocators);
    }

    public static class Builder {

        private final List<PluginLocator> pluginLocators = new ArrayList<>();
        private final List<DependencyDescriptor> dependencyDescriptors = new ArrayList<>();

        private Builder() {}

        public ClockworkConfig build() {
            return new ClockworkConfig(this);
        }

        public void addPluginLocator(PluginLocator locator) {
            if (pluginLocators.stream().anyMatch(d -> d.getName().equals(locator.getName())))
                throw new IllegalArgumentException("duplicate locator: " + locator.getName());
            this.pluginLocators.add(locator);
        }

        public void addComponentDescriptor(DependencyDescriptor descriptor) {
            if (dependencyDescriptors.stream().anyMatch(d -> d.getTarget().equals(descriptor.getTarget())))
                throw new IllegalArgumentException("duplicate component descriptor: " + descriptor.getTarget());
            this.dependencyDescriptors.add(descriptor);
        }

    }

}
