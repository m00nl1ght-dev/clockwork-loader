package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.core.LoadingContext;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.fnder.PluginFinderConfig.Builder;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.reader.PluginReaderUtil;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.Registry;
import dev.m00nl1ght.clockwork.util.config.ImmutableConfig;
import dev.m00nl1ght.clockwork.version.Version;

import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class NestedPluginFinder extends AbstractIndexedPluginFinder {

    public static final String NAME = "internal.pluginfinder.nested";
    public static final PluginFinderType FACTORY = NestedPluginFinder::new;

    private static final String DEFAULT_PATH_IN_MODULE = "libs/";

    protected final PluginFinderConfig innerFinderConfig;
    protected final String pathInModule;

    private PluginFinder innerFinder;
    private Path tempDir;

    public static void registerTo(Registry<PluginFinderType> registry) {
        Arguments.notNull(registry, "registry");
        registry.register(NAME, FACTORY);
    }

    public static Builder configBuilder(String name, PluginFinderConfig innerFinder) {
        return configBuilder(name, innerFinder, DEFAULT_PATH_IN_MODULE);
    }

    public static Builder configBuilder(String name, PluginFinderConfig innerFinder, String pathInModule) {
        return PluginFinderConfig.builder(name, NAME)
                .withParams(ImmutableConfig.builder()
                        .put("innerFinder", innerFinder.asRaw())
                        .put("pathInModule", pathInModule)
                        .build());
    }

    protected NestedPluginFinder(PluginFinderConfig config) {
        super(config);
        this.innerFinderConfig = PluginFinderConfig.from(config.getParams().getSubconfig("innerFinder"));
        this.pathInModule = config.getParams().getOrDefault("pathInModule", DEFAULT_PATH_IN_MODULE);
    }

    @Override
    protected Set<String> indexPlugins(LoadingContext context) {
        return innerFinder(context).getAvailablePlugins(context);
    }

    @Override
    protected Set<Version> indexVersions(LoadingContext context, String pluginId) {
        return innerFinder(context).getAvailableVersions(context, pluginId);
    }

    @Override
    protected Optional<PluginReference> find(LoadingContext context, Collection<PluginReader> readers, String pluginId, Version version) {
        final var ref = innerFinder(context).find(context, pluginId, version);
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

    protected PluginFinder innerFinder(LoadingContext context) {
        if (innerFinder != null) return innerFinder;
        innerFinder = context.getFinderType(innerFinderConfig.getType()).build(innerFinderConfig);
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

}
