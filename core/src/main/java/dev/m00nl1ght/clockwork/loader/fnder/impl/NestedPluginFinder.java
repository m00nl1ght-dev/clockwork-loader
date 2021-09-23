package dev.m00nl1ght.clockwork.loader.fnder.impl;

import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.loader.reader.impl.PluginReaderUtil;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;
import dev.m00nl1ght.clockwork.utils.version.Version;

import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class NestedPluginFinder extends AbstractIndexedPluginFinder {

    public static final String TYPE = "internal.pluginfinder.nested";

    private static final String DEFAULT_PATH_IN_MODULE = "libs/";

    protected final Config innerFinderConfig;
    protected final String pathInModule;

    private PluginFinder innerFinder;
    private Path tempDir;

    public static void registerTo(ClockworkLoader loader) {
        loader.getFeatureProviders().register(PluginFinder.class, TYPE, NestedPluginFinder::new);
    }

    public static ModifiableConfig newConfig(String name, Config innerFinder, boolean wildcard) {
        return newConfig(name, innerFinder, DEFAULT_PATH_IN_MODULE, null, wildcard);
    }

    public static ModifiableConfig newConfig(String name, Config innerFinder, String pathInModule, List<String> readers, boolean wildcard) {
        return Config.newConfig()
                .putString("type", TYPE)
                .putString("name", name)
                .putStrings("readers", readers)
                .putString("wildcard", wildcard)
                .putSubconfig("innerFinder", innerFinder)
                .putString("pathInModule", pathInModule);
    }

    protected NestedPluginFinder(ClockworkLoader loader, Config config) {
        super(loader, config);
        this.innerFinderConfig = config.getSubconfig("innerFinder");
        this.pathInModule = config.getOrDefault("pathInModule", Config.STRING, DEFAULT_PATH_IN_MODULE);
    }

    @Override
    protected Set<String> indexPlugins(ClockworkLoader loader) {
        return innerFinder(loader).getAvailablePlugins(loader);
    }

    @Override
    protected Set<Version> indexVersions(ClockworkLoader loader, String pluginId) {
        return innerFinder(loader).getAvailableVersions(loader, pluginId);
    }

    @Override
    protected Optional<PluginReference> find(ClockworkLoader loader, Collection<PluginReader> readers, String pluginId, Version version) {
        final var ref = innerFinder(loader).find(loader, pluginId, version);
        return ref.flatMap(r -> r.getModuleFinder().find(ref.get().getModuleName()))
                .flatMap(ModuleReference::location)
                .flatMap(uri -> PluginReaderUtil.tryAsModuleRoot(Path.of(uri), path -> {
                    final var innerPath = path.resolve(pathInModule);
                    if (!Files.isDirectory(innerPath)) return ref;
                    final var libPaths = copyToTemp(innerPath);
                    if (libPaths.isEmpty()) return ref;
                    final var libFinder = ModuleFinder.of(libPaths.toArray(Path[]::new));
                    return Optional.of(PluginReference.of(ref.get(), libFinder));
                }));
    }

    protected PluginFinder innerFinder(ClockworkLoader loader) {
        if (innerFinder != null) return innerFinder;
        innerFinder = loader.getFeatureProviders().newFeature(PluginFinder.class, loader, innerFinderConfig);
        return innerFinder;
    }

    protected Path tempDir() {
        if (tempDir != null) return tempDir;
        try {
            tempDir = Files.createTempDirectory("cwl-temp");
            return tempDir;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp dir", e);
        }
    }

    protected Set<Path> copyToTemp(Path path) {
        try {
            final var resPaths = new HashSet<Path>();
            final var files = Files.list(path).filter(Files::isRegularFile).collect(Collectors.toUnmodifiableSet());
            for (final var file : files) {
                final var resPath = tempDir().resolve(file.getFileName());
                Files.copy(file, resPath);
                resPaths.add(resPath);
            }
            return resPaths;
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy to temp dir from: " + path);
        }
    }

    @Override
    public String toString() {
        return TYPE + "[" + name +  "]";
    }

}
