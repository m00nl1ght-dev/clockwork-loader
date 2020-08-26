package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.locator.PluginLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClockworkConfig {

    private final List<PluginLocator> pluginLocators = new ArrayList<>();
    private final List<DependencyDescriptor> wantedPlugins = new ArrayList<>();

    public static Builder builder() {
        return new Builder();
    }

    public ClockworkConfig(Builder builder) {
        this.pluginLocators.addAll(builder.pluginLocators);
        this.wantedPlugins.addAll(builder.wantedPlugins);
    }

    public List<DependencyDescriptor> getWantedPlugins() {
        return Collections.unmodifiableList(wantedPlugins);
    }

    public List<PluginLocator> getPluginLocators() {
        return Collections.unmodifiableList(pluginLocators);
    }

    public static class Builder {

        private final List<PluginLocator> pluginLocators = new ArrayList<>();
        private final List<DependencyDescriptor> wantedPlugins = new ArrayList<>();

        private Builder() {}

        public ClockworkConfig build() {
            return new ClockworkConfig(this);
        }

        public void addPluginLocator(PluginLocator locator) {
            if (pluginLocators.stream().anyMatch(d -> d.getName().equals(locator.getName())))
                throw new IllegalArgumentException("duplicate locator: " + locator.getName());
            this.pluginLocators.add(locator);
        }

        public void addWantedPlugin(DependencyDescriptor descriptor) {
            if (!descriptor.getComponent().isEmpty()) throw new IllegalArgumentException("not a plugin id");
            if (wantedPlugins.stream().anyMatch(d -> d.getPlugin().equals(descriptor.getPlugin())))
                throw new IllegalArgumentException("duplicate wanted plugin: " + descriptor.getPlugin());
            this.wantedPlugins.add(descriptor);
        }

    }

}
