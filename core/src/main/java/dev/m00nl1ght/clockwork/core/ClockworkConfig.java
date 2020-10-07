package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.fnder.PluginFinderConfig;
import dev.m00nl1ght.clockwork.reader.PluginReaderConfig;
import dev.m00nl1ght.clockwork.util.ImmutableConfig;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class ClockworkConfig extends ImmutableConfig {

    private final List<DependencyDescriptor> wantedPlugins;
    private final Set<PluginFinderConfig> finders;
    private final Set<PluginReaderConfig> readers;

    public static Builder builder() {
        return new Builder();
    }

    private ClockworkConfig(Builder builder) {
        super(builder);
        this.wantedPlugins = List.copyOf(builder.wantedPlugins);
        this.finders = Set.copyOf(builder.finders);
        this.readers = Set.copyOf(builder.readers);
    }

    public List<DependencyDescriptor> getWantedPlugins() {
        return wantedPlugins;
    }

    public Set<PluginReaderConfig> getReaders() {
        return readers;
    }

    public Set<PluginFinderConfig> getFinders() {
        return finders;
    }

    public static class Builder extends ImmutableConfig.Builder {

        private final List<DependencyDescriptor> wantedPlugins = new LinkedList<>();
        private final Set<PluginFinderConfig> finders = new LinkedHashSet<>();
        private final Set<PluginReaderConfig> readers = new LinkedHashSet<>();

        private Builder() {
            configName("ClockworkConfig");
        }

        @Override
        public ClockworkConfig build() {
            return new ClockworkConfig(this);
        }

        public void addPluginFinder(PluginFinderConfig finder) {
            if (finders.stream().anyMatch(d -> d.getName().equals(finder.getName())))
                throw new IllegalArgumentException("duplicate finder: " + finder.getName());
            this.finders.add(finder);
        }

        public void addPluginReader(PluginReaderConfig reader) {
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
