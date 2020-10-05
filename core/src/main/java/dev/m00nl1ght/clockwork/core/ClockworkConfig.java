package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.locator.LocatorConfig;
import dev.m00nl1ght.clockwork.reader.ReaderConfig;
import dev.m00nl1ght.clockwork.util.ImmutableConfig;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class ClockworkConfig extends ImmutableConfig {

    private final List<DependencyDescriptor> wantedPlugins;
    private final Set<LocatorConfig> locators;
    private final Set<ReaderConfig> readers;

    public static Builder builder() {
        return new Builder();
    }

    private ClockworkConfig(Builder builder) {
        super(builder);
        this.wantedPlugins = List.copyOf(builder.wantedPlugins);
        this.locators = Set.copyOf(builder.locators);
        this.readers = Set.copyOf(builder.readers);
    }

    public List<DependencyDescriptor> getWantedPlugins() {
        return wantedPlugins;
    }

    public Set<ReaderConfig> getReaders() {
        return readers;
    }

    public Set<LocatorConfig> getLocators() {
        return locators;
    }

    public static class Builder extends ImmutableConfig.Builder {

        private final List<DependencyDescriptor> wantedPlugins = new LinkedList<>();
        private final Set<LocatorConfig> locators = new LinkedHashSet<>();
        private final Set<ReaderConfig> readers = new LinkedHashSet<>();

        private Builder() {
            configName("ClockworkConfig");
        }

        @Override
        public ClockworkConfig build() {
            return new ClockworkConfig(this);
        }

        public void addPluginLocator(LocatorConfig locator) {
            if (locators.stream().anyMatch(d -> d.getName().equals(locator.getName())))
                throw new IllegalArgumentException("duplicate locator: " + locator.getName());
            this.locators.add(locator);
        }

        public void addPluginReader(ReaderConfig reader) {
            if (readers.stream().anyMatch(d -> d.getName().equals(reader.getName())))
                throw new IllegalArgumentException("duplicate reader: " + reader.getName());
            this.readers.add(reader);
        }

        public void addWantedPlugin(DependencyDescriptor descriptor) {
            if (!descriptor.getComponent().isEmpty()) throw new IllegalArgumentException("not a plugin id");
            if (wantedPlugins.stream().anyMatch(d -> d.getPlugin().equals(descriptor.getPlugin())))
                throw new IllegalArgumentException("duplicate wanted plugin: " + descriptor.getPlugin());
            this.wantedPlugins.add(descriptor);
        }

    }

}
