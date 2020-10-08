package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.core.LoadingContext;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.Registry;

import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class NestedPluginFinder extends AbstractPluginFinder {

    public static final String NAME = "internal.pluginfinder.nested";
    public static final PluginFinderType FACTORY = NestedPluginFinder::new;

    private static final String DEFAULT_PATH_IN_MODULE = "libs/";

    protected final Set<String> sourceFinders;
    protected final String pathInModule;
    protected final boolean loadPluginsAsLibs;

    protected ModuleFinder moduleFinder;
    protected Path tempDir;

    public static void registerTo(Registry<PluginFinderType> registry) {
        Arguments.notNull(registry, "registry");
        registry.register(NAME, FACTORY);
    }

    public static PluginFinderConfig newConfig(String name, Set<String> inFinders) {
        return newConfig(name, inFinders, false);
    }

    public static PluginFinderConfig newConfig(String name, Set<String> inFinders, boolean wildcard) {
        return newConfig(name, inFinders, DEFAULT_PATH_IN_MODULE, wildcard);
    }

    public static PluginFinderConfig newConfig(String name, Set<String> inFinders, String pathInModule, boolean wildcard) {
        return newConfig(name, inFinders, pathInModule, false, null, wildcard);
    }

    public static PluginFinderConfig newConfig(String name, Set<String> inFinders, String pathInModule,
                                               boolean loadPluginsAsLibs, Set<String> readers, boolean wildcard) {
        return new PluginFinderConfig(name, NAME,
                Map.of("inFinders", String.join(",", inFinders),
                        "pathInModule", pathInModule,
                        "loadPluginsAsLibs", String.valueOf(loadPluginsAsLibs)), readers, wildcard);
    }

    protected NestedPluginFinder(PluginFinderConfig config) {
        super(config);
        this.pathInModule = config.getOrDefault("pathInModule", DEFAULT_PATH_IN_MODULE);
        this.loadPluginsAsLibs = config.getBooleanOrDefault("loadPluginsAsLibs", false);
        this.sourceFinders = Arrays.stream(config.get("inFinders").split(","))
                .map(String::trim).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected void scan(LoadingContext context, Collection<PluginReader> readers) {
        for (final var sourceName : sourceFinders) {
            final var source = context.getFinder(sourceName);
            for (final var module : source.findAll(context)) {
                module.getMainModule().location().ifPresent(uri -> tryAsModuleRoot(Path.of(uri), path -> {
                    final var innerPath = path.resolve(pathInModule);
                    if (Files.isDirectory(innerPath)) copyToTemp(innerPath);
                    return Optional.empty();
                }));
            }
        }
        if (moduleFinder != null && !loadPluginsAsLibs) {
            moduleFinder.findAll().stream()
                    .map(m -> tryReadFromModule(readers, m, moduleFinder))
                    .filter(Optional::isPresent).map(Optional::get)
                    .forEach(this::found);
        }
    }

    protected void copyToTemp(Path path) {
        try {
            final var files = Files.list(path).filter(Files::isRegularFile).collect(Collectors.toUnmodifiableSet());
            for (final var file : files) Files.copy(file, tempDir().resolve(file.getFileName()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy to temp dir from: " + path);
        }
    }

    protected Path tempDir() {
        if (tempDir != null) return tempDir;
        try {
            tempDir = Files.createTempDirectory("cwl-temp");
            tempDir.toFile().deleteOnExit();
            moduleFinder = ModuleFinder.of(tempDir);
            return tempDir;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp dir", e);
        }
    }

    @Override
    public ModuleFinder getModuleFinder(LoadingContext context) {
        scanIfNeeded(context);
        return moduleFinder;
    }

}
