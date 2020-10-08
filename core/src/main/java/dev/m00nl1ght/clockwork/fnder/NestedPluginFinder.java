package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.core.LoadingContext;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.util.Arguments;

import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class NestedPluginFinder extends AbstractPluginFinder {

    public static final String NAME = "internal.pluginfinder.nested";
    public static final PluginFinderType FACTORY = NestedPluginFinder::new;

    protected final Set<String> sourceFinders;
    protected final String pathInModule;

    protected ModuleFinder moduleFinder;
    protected Path tempDir;

    public static void registerTo(ClockworkLoader loader) {
        Arguments.notNull(loader, "loader");
        loader.registerFinderType(NAME, FACTORY);
    }

    public static void registerTo(CollectClockworkExtensionsEvent event) {
        Arguments.notNull(event, "event");
        event.registerLocatorFactory(NAME, FACTORY);
    }

    public static PluginFinderConfig newConfig(String name) {
        return newConfig(name, false);
    }

    public static PluginFinderConfig newConfig(String name, boolean wildcard) {
        return newConfig(name, null, wildcard);
    }

    public static PluginFinderConfig newConfig(String name, Set<String> readers, boolean wildcard) {
        return new PluginFinderConfig(name, NAME, Map.of(), readers, wildcard);
    }

    protected NestedPluginFinder(PluginFinderConfig config) {
        super(config);
        this.pathInModule = config.get("pathInModule");
        this.sourceFinders = Arrays.stream(config.get("inFinder").split(","))
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
        if (moduleFinder != null) moduleFinder.findAll().stream()
                .map(m -> tryReadFromModule(readers, m, moduleFinder))
                .filter(Optional::isPresent).map(Optional::get)
                .forEach(this::found);
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
