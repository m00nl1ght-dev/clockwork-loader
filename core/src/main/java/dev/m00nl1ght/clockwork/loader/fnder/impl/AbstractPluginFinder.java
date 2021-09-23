package dev.m00nl1ght.clockwork.loader.fnder.impl;

import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.utils.logger.FormatUtil;
import dev.m00nl1ght.clockwork.utils.version.Version;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractPluginFinder implements PluginFinder {

    private Map<String, Optional<PluginReference>> cache;

    protected final String name;
    protected final List<String> readerNames;
    protected final boolean wildcard;

    protected AbstractPluginFinder(ClockworkLoader loader, Config config) {
        this.name = config.getRequired("name", Config.STRING);
        this.readerNames = config.getStrings("readers");
        this.wildcard = config.getOrDefault("wildcard", Config.BOOLEAN, false);
    }

    @Override
    public Set<String> getAvailablePlugins(ClockworkLoader loader) {
        scanIfNeeded(loader);
        return Set.copyOf(cache.keySet());
    }

    @Override
    public Set<Version> getAvailableVersions(ClockworkLoader loader, String pluginId) {
        scanIfNeeded(loader);
        final var ref = cache.getOrDefault(pluginId, Optional.empty());
        return ref.isEmpty() ? Collections.emptySet() : Collections.singleton(ref.get().getVersion());
    }

    @Override
    public Optional<PluginReference> find(ClockworkLoader loader, String pluginId, Version version) {
        scanIfNeeded(loader);
        final var ref = cache.getOrDefault(pluginId, Optional.empty());
        return ref.isPresent() && ref.get().getVersion().equals(version) ? ref : Optional.empty();
    }

    @Override
    public boolean isWildcard() {
        return wildcard;
    }

    protected void scanIfNeeded(ClockworkLoader loader) {
        if (cache != null) return;
        final var readers = readerNames == null
                ? loader.getFeatures().getAll(PluginReader.class)
                : loader.getFeatures().getAll(PluginReader.class, readerNames);
        cache = scan(loader, readers).stream()
                .collect(Collectors.groupingBy(PluginReference::getId,
                        Collectors.reducing(this::onDuplicate)));
    }

    protected abstract Set<PluginReference> scan(ClockworkLoader loader, Collection<PluginReader> readers);

    protected PluginReference onDuplicate(PluginReference a, PluginReference b) {
        throw FormatUtil.rtExc("[] found multiple versions of the same plugin: [] and []", this, a, b);
    }

}
