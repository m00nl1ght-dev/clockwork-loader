package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.locator.PluginLocator;

import java.util.ArrayList;
import java.util.List;

public final class ClockworkConfig {

    private final List<PluginLocator> pluginLocators;
    private final List<DependencyDescriptor> wantedPlugins;
    private final List<PluginLocator> wantedWildcard;

    public static Builder builder() {
        return new Builder();
    }

    public ClockworkConfig(Builder builder) {
        this.pluginLocators = List.copyOf(builder.pluginLocators);
        this.wantedPlugins = List.copyOf(builder.wantedPlugins);
        this.wantedWildcard = List.copyOf(builder.wantedWildcard);
    }

    public List<PluginLocator> getPluginLocators() {
        return pluginLocators;
    }

    public List<DependencyDescriptor> getWantedPlugins() {
        return wantedPlugins;
    }

    public List<PluginLocator> getWantedWildcard() {
        return wantedWildcard;
    }

    public static class Builder {

        private final List<PluginLocator> pluginLocators = new ArrayList<>();
        private final List<DependencyDescriptor> wantedPlugins = new ArrayList<>();
        private final List<PluginLocator> wantedWildcard = new ArrayList<>();

        private Builder() {}

        public ClockworkConfig build() {
            return new ClockworkConfig(this);
        }

        public void addPluginLocator(PluginLocator locator) {
            this.addPluginLocator(locator, false);
        }

        public void addPluginLocator(PluginLocator locator, boolean allWanted) {
            if (pluginLocators.stream().anyMatch(d -> d.getName().equals(locator.getName())))
                throw new IllegalArgumentException("duplicate locator: " + locator.getName());
            this.pluginLocators.add(locator);
            if (allWanted) this.wantedWildcard.add(locator);
        }

        public void addWantedPlugin(DependencyDescriptor descriptor) {
            if (!descriptor.getComponent().isEmpty()) throw new IllegalArgumentException("not a plugin id");
            if (wantedPlugins.stream().anyMatch(d -> d.getPlugin().equals(descriptor.getPlugin())))
                throw new IllegalArgumentException("duplicate wanted plugin: " + descriptor.getPlugin());
            this.wantedPlugins.add(descriptor);
        }

    }

}
