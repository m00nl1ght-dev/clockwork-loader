package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.reader.PluginReader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LoadingContext {

    protected final ClockworkConfig config;
    protected final Map<String, PluginReader> readers;
    protected final Map<String, PluginFinder> finders;

    public static LoadingContext of(ClockworkConfig clockworkConfig, ClockworkLoader loader) {

        final var readers = new HashMap<String, PluginReader>();
        for (final var config : clockworkConfig.getReaders()) {
            final var reader = loader.getReaderType(config).build(config);
            readers.put(config.getName(), reader);
        }

        final var finders = new HashMap<String, PluginFinder>();
        for (final var config : clockworkConfig.getFinders()) {
            final var finder = loader.getFinderType(config).build(config);
            finders.put(config.getName(), finder);
        }

        return new LoadingContext(clockworkConfig, readers, finders);

    }

    protected LoadingContext(ClockworkConfig config, Map<String, PluginReader> readers, Map<String, PluginFinder> finders) {
        this.config = config;
        this.readers = Map.copyOf(readers);
        this.finders = Map.copyOf(finders);
    }

    public ClockworkConfig getConfig() {
        return config;
    }

    public Collection<PluginFinder> getFinders() {
        return finders.values();
    }

    public PluginFinder getFinder(String name) {
        final var finder = finders.get(name);
        if (finder == null) throw PluginLoadingException.missingFinder(name);
        return finder;
    }

    public Collection<PluginReader> getReaders() {
        return readers.values();
    }

    public PluginReader getReader(String name) {
        final var reader = readers.get(name);
        if (reader == null) throw PluginLoadingException.missingReader(name);
        return reader;
    }

}
