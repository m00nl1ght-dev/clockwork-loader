package dev.m00nl1ght.clockwork.loader;

import dev.m00nl1ght.clockwork.loader.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinderType;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.loader.reader.PluginReaderType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LoadingContext {

    private final ClockworkConfig config;
    private final ExtensionContext extensionContext;
    private final Map<String, PluginReader> readers;
    private final Map<String, PluginFinder> finders;

    public static @NotNull LoadingContext of(
            @NotNull ClockworkConfig clockworkConfig,
            @NotNull ExtensionContext extensionContext) {

        final var readers = new HashMap<String, PluginReader>();
        for (final var config : clockworkConfig.getReaders()) {
            final var reader = extensionContext.get(config.getType(), PluginReaderType.class).build(config);
            readers.put(config.getName(), reader);
        }

        final var finders = new HashMap<String, PluginFinder>();
        for (final var config : clockworkConfig.getFinders()) {
            final var finder = extensionContext.get(config.getType(), PluginFinderType.class).build(config);
            finders.put(config.getName(), finder);
        }

        return new LoadingContext(clockworkConfig, extensionContext, readers, finders);

    }

    protected LoadingContext(
            @NotNull ClockworkConfig config,
            @NotNull ExtensionContext extensionContext,
            @NotNull Map<String, PluginReader> readers,
            @NotNull Map<String, PluginFinder> finders) {

        this.config = config;
        this.extensionContext = extensionContext;
        this.readers = Map.copyOf(readers);
        this.finders = Map.copyOf(finders);
    }

    public @NotNull ClockworkConfig getConfig() {
        return config;
    }

    public @NotNull Collection<@NotNull PluginFinder> getFinders() {
        return finders.values();
    }

    public @NotNull PluginFinder getFinder(@NotNull String name) {
        final var finder = finders.get(name);
        if (finder == null) throw PluginLoadingException.missingFinder(name);
        return finder;
    }

    public @NotNull Collection<@NotNull PluginReader> getReaders() {
        return readers.values();
    }

    public @NotNull PluginReader getReader(@NotNull String name) {
        final var reader = readers.get(name);
        if (reader == null) throw PluginLoadingException.missingReader(name);
        return reader;
    }

    public @NotNull ExtensionContext getExtensionContext() {
        return extensionContext;
    }

}
