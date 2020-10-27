package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.fnder.PluginFinderConfig;
import dev.m00nl1ght.clockwork.reader.PluginReaderConfig;
import dev.m00nl1ght.clockwork.config.Config;
import dev.m00nl1ght.clockwork.verifier.PluginVerifierConfig;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ClockworkConfig {

    private final List<DependencyDescriptor> wantedPlugins;
    private final Set<PluginFinderConfig> finders;
    private final Set<PluginReaderConfig> readers;
    private final Set<PluginVerifierConfig> verifiers;
    private final Set<Path> libModulePath;

    private ClockworkConfig(Builder builder) {
        this.wantedPlugins = List.copyOf(builder.wantedPlugins);
        this.finders = Set.copyOf(builder.finders);
        this.readers = Set.copyOf(builder.readers);
        this.verifiers = Set.copyOf(builder.verifiers);
        this.libModulePath = Set.copyOf(builder.libModulePath);
    }

    private ClockworkConfig(Config data) {
        this.wantedPlugins = List.copyOf(data.getListOrEmpty("plugins")
                .stream().map(DependencyDescriptor::build)
                .collect(Collectors.toUnmodifiableList()));
        this.finders = Set.copyOf(data.getSubconfigListOrEmpty("finders")
                .stream().map(PluginFinderConfig::from)
                .collect(Collectors.toUnmodifiableSet()));
        this.readers = Set.copyOf(data.getSubconfigListOrEmpty("readers")
                .stream().map(PluginReaderConfig::from)
                .collect(Collectors.toUnmodifiableSet()));
        this.verifiers = Set.copyOf(data.getSubconfigListOrEmpty("verifiers")
                .stream().map(PluginVerifierConfig::from)
                .collect(Collectors.toUnmodifiableSet()));
        this.libModulePath = Set.copyOf(data.getListOrSingletonOrEmpty("libModulePath")
                .stream().map(Path::of)
                .collect(Collectors.toUnmodifiableSet()));
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

    public Set<PluginVerifierConfig> getVerifiers() {
        return verifiers;
    }

    public Set<Path> getLibModulePath() {
        return libModulePath;
    }

    public static ClockworkConfig from(Config data) {
        return new ClockworkConfig(data);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<DependencyDescriptor> wantedPlugins = new LinkedList<>();
        private final Set<PluginFinderConfig> finders = new LinkedHashSet<>();
        private final Set<PluginReaderConfig> readers = new LinkedHashSet<>();
        private final Set<PluginVerifierConfig> verifiers = new LinkedHashSet<>();
        private final Set<Path> libModulePath = new LinkedHashSet<>();

        private Builder() {}

        public ClockworkConfig build() {
            return new ClockworkConfig(this);
        }

        public Builder addPluginFinder(PluginFinderConfig finder) {
            if (finders.stream().anyMatch(d -> d.getName().equals(finder.getName())))
                throw new IllegalArgumentException("duplicate finder: " + finder.getName());
            this.finders.add(finder);
            return this;
        }

        public Builder addPluginReader(PluginReaderConfig reader) {
            if (readers.stream().anyMatch(d -> d.getName().equals(reader.getName())))
                throw new IllegalArgumentException("duplicate reader: " + reader.getName());
            this.readers.add(reader);
            return this;
        }

        public Builder addPluginVerifier(PluginVerifierConfig verifier) {
            if (verifiers.stream().anyMatch(d -> d.getName().equals(verifier.getName())))
                throw new IllegalArgumentException("duplicate verifier: " + verifier.getName());
            this.verifiers.add(verifier);
            return this;
        }

        public Builder addWantedPlugin(DependencyDescriptor descriptor) {
            if (!descriptor.getComponent().isEmpty()) throw new IllegalArgumentException("not a plugin id");
            if (wantedPlugins.stream().anyMatch(d -> d.getPlugin().equals(descriptor.getPlugin())))
                throw new IllegalArgumentException("duplicate wanted plugin: " + descriptor.getPlugin());
            this.wantedPlugins.add(descriptor);
            return this;
        }

        public Builder addToLibModulePath(Path modulePath) {
            this.libModulePath.add(modulePath);
            return this;
        }

    }

}
