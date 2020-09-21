package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.locator.LocatorConfig;
import dev.m00nl1ght.clockwork.util.ImmutableConfig;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class ClockworkConfig extends ImmutableConfig {

    private final List<DependencyDescriptor> wantedPlugins;
    private final Set<LocatorConfig> locators;

    public static Builder builder() {
        return new Builder();
    }

    private ClockworkConfig(Builder builder) {
        super(builder);
        this.wantedPlugins = List.copyOf(builder.wantedPlugins);
        this.locators = Set.copyOf(builder.locators);
    }

    public List<DependencyDescriptor> getWantedPlugins() {
        return wantedPlugins;
    }

    public Set<LocatorConfig> getLocators() {
        return locators;
    }

    public static class Builder extends ImmutableConfig.Builder {

        private final List<DependencyDescriptor> wantedPlugins = new LinkedList<>();
        private final Set<LocatorConfig> locators = new LinkedHashSet<>();

        private Builder() {}

        @Override
        public ClockworkConfig build() {
            return new ClockworkConfig(this);
        }

        public void addPluginLocator(LocatorConfig locator) {
            if (locators.stream().anyMatch(d -> d.getLocator().equals(locator.getLocator())))
                throw new IllegalArgumentException("duplicate locator: " + locator);
            this.locators.add(locator);
        }

        public void addWantedPlugin(DependencyDescriptor descriptor) {
            if (!descriptor.getComponent().isEmpty()) throw new IllegalArgumentException("not a plugin id");
            if (wantedPlugins.stream().anyMatch(d -> d.getPlugin().equals(descriptor.getPlugin())))
                throw new IllegalArgumentException("duplicate wanted plugin: " + descriptor.getPlugin());
            this.wantedPlugins.add(descriptor);
        }

    }

}
