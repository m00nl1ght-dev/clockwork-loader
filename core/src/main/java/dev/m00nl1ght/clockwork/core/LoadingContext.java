package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.verifier.PluginVerifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LoadingContext {

    private final ClockworkConfig config;
    private final ExtensionContext extensionContext;
    private final Map<String, PluginReader> readers;
    private final Map<String, PluginFinder> finders;
    private final Map<String, PluginVerifier> verifiers;

    public static @NotNull LoadingContext of(
            @NotNull ClockworkConfig clockworkConfig,
            @NotNull ExtensionContext extensionContext) {

        final var readers = new HashMap<String, PluginReader>();
        for (final var config : clockworkConfig.getReaders()) {
            final var reader = extensionContext.getReaderTypeRegistry().get(config.getType()).build(config);
            readers.put(config.getName(), reader);
        }

        final var finders = new HashMap<String, PluginFinder>();
        for (final var config : clockworkConfig.getFinders()) {
            final var finder = extensionContext.getFinderTypeRegistry().get(config.getType()).build(config);
            finders.put(config.getName(), finder);
        }

        final var verifiers = new HashMap<String, PluginVerifier>();
        for (final var config : clockworkConfig.getVerifiers()) {
            final var verifier = extensionContext.getVerifierTypeRegistry().get(config.getType()).build(config);
            verifiers.put(config.getName(), verifier);
        }

        return new LoadingContext(clockworkConfig, extensionContext, readers, finders, verifiers);

    }

    protected LoadingContext(
            @NotNull ClockworkConfig config,
            @NotNull ExtensionContext extensionContext,
            @NotNull Map<String, PluginReader> readers,
            @NotNull Map<String, PluginFinder> finders,
            @NotNull Map<String, PluginVerifier> verifiers) {

        this.config = config;
        this.extensionContext = extensionContext;
        this.readers = Map.copyOf(readers);
        this.finders = Map.copyOf(finders);
        this.verifiers = Map.copyOf(verifiers);
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

    public @NotNull Collection<@NotNull PluginVerifier> getVerifiers() {
        return verifiers.values();
    }

    public @NotNull PluginVerifier getVerifier(@NotNull String name) {
        final var verifier = verifiers.get(name);
        if (verifier == null) throw PluginLoadingException.missingVerifier(name);
        return verifier;
    }

    public @NotNull ExtensionContext getExtensionContext() {
        return extensionContext;
    }

}
