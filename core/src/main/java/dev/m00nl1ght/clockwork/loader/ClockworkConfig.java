package dev.m00nl1ght.clockwork.loader;

import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.loader.jigsaw.impl.JigsawStrategyFlat;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class ClockworkConfig {

    private final List<DependencyDescriptor> wantedPlugins;
    private final Set<Config> finders;
    private final Set<Config> readers;
    private final Set<Config> transformers;
    private final Config jigsawStrategy;
    private final Set<Path> libModulePath;
    private final Config extConfig;

    private ClockworkConfig(Builder builder) {
        this.wantedPlugins = List.copyOf(builder.wantedPlugins);
        this.finders = Set.copyOf(builder.finders);
        this.readers = Set.copyOf(builder.readers);
        this.transformers = Set.copyOf(builder.transformers);
        this.libModulePath = Set.copyOf(builder.libModulePath);
        this.jigsawStrategy = builder.jigsawStrategy;
        this.extConfig = builder.extConfig;
    }

    private ClockworkConfig(Config data) {
        this.wantedPlugins = List.copyOf(data.getOrDefault("plugins", Config.LIST, List.of())
                .stream().map(DependencyDescriptor::build)
                .collect(Collectors.toUnmodifiableList()));
        this.finders = Set.copyOf(data.getOrDefault("finders", Config.CLIST, List.of()));
        this.readers = Set.copyOf(data.getOrDefault("readers", Config.CLIST, List.of()));
        this.transformers = Set.copyOf(data.getOrDefault("transformers", Config.CLIST, List.of()));
        this.libModulePath = Set.copyOf(data.getOrDefault("libModulePath", Config.LISTF, List.of())
                .stream().map(Path::of)
                .collect(Collectors.toUnmodifiableSet()));
        this.jigsawStrategy = data.getOrDefault("jigsawStrategy", Config.CONFIG, Config.EMPTY);
        this.extConfig = data.getOrDefault("ext", Config.CONFIG, Config.EMPTY);
    }

    public List<DependencyDescriptor> getWantedPlugins() {
        return wantedPlugins;
    }

    public Set<Config> getFinders() {
        return finders;
    }

    public Set<Config> getReaders() {
        return readers;
    }

    public Set<Config> getTransformers() {
        return transformers;
    }

    public Config getJigsawStrategy() {
        return jigsawStrategy;
    }

    public Set<Path> getLibModulePath() {
        return libModulePath;
    }

    public Config getExtConfig() {
        return extConfig;
    }

    public static ClockworkConfig from(Config data) {
        return new ClockworkConfig(data);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<DependencyDescriptor> wantedPlugins = new LinkedList<>();
        private final Set<Config> finders = new LinkedHashSet<>();
        private final Set<Config> readers = new LinkedHashSet<>();
        private final Set<Config> transformers = new LinkedHashSet<>();
        private final Set<Path> libModulePath = new LinkedHashSet<>();
        private Config extConfig = Config.EMPTY;
        private Config jigsawStrategy;

        private Builder() {}

        public ClockworkConfig build() {
            if (jigsawStrategy == null) jigsawStrategy = JigsawStrategyFlat.newConfig("default");
            return new ClockworkConfig(this);
        }

        public Builder addPluginFinder(Config finder) {
            this.finders.add(finder);
            return this;
        }

        public Builder addPluginReader(Config reader) {
            this.readers.add(reader);
            return this;
        }

        public Builder addClassTransformer(Config transformer) {
            this.transformers.add(transformer);
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

        public void setJigsawStrategy(Config jigsawConfig) {
            this.jigsawStrategy = Objects.requireNonNull(jigsawConfig);
        }

        public void extConfig(Config extConfig) {
            this.extConfig = Objects.requireNonNull(extConfig).copy();
        }

    }

}
