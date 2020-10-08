package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.verifier.PluginVerifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LoadingContext {

    protected final ClockworkConfig config;
    protected final Map<String, PluginReader> readers;
    protected final Map<String, PluginFinder> finders;
    protected final Map<String, PluginVerifier> verifiers;

    public static LoadingContext of(ClockworkConfig clockworkConfig, ClockworkLoader loader) {

        final var readers = new HashMap<String, PluginReader>();
        for (final var config : clockworkConfig.getReaders()) {
            final var reader = loader.getReaderTypeRegistry().get(config.getType()).build(config);
            readers.put(config.getName(), reader);
        }

        final var finders = new HashMap<String, PluginFinder>();
        for (final var config : clockworkConfig.getFinders()) {
            final var finder = loader.getFinderTypeRegistry().get(config.getType()).build(config);
            finders.put(config.getName(), finder);
        }

        final var verifiers = new HashMap<String, PluginVerifier>();
        for (final var config : clockworkConfig.getVerifiers()) {
            final var verifier = loader.getVerifierTypeRegistry().get(config.getType()).build(config);
            verifiers.put(config.getName(), verifier);
        }

        return new LoadingContext(clockworkConfig, readers, finders, verifiers);

    }

    protected LoadingContext(ClockworkConfig config, Map<String, PluginReader> readers, Map<String, PluginFinder> finders, HashMap<String, PluginVerifier> verifiers) {
        this.config = config;
        this.readers = Map.copyOf(readers);
        this.finders = Map.copyOf(finders);
        this.verifiers = Map.copyOf(verifiers);
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

    public Collection<PluginVerifier> getVerifiers() {
        return verifiers.values();
    }

    public PluginVerifier getVerifier(String name) {
        final var verifier = verifiers.get(name);
        if (verifier == null) throw PluginLoadingException.missingVerifier(name);
        return verifier;
    }

}
