package dev.m00nl1ght.clockwork.loader.fnder.impl;

import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.version.Version;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractScanningPluginFinder extends AbstractPluginFinder {

    private Map<String, List<PluginReference>> cache;

    protected AbstractScanningPluginFinder(ClockworkLoader loader, Config config) {
        super(loader, config);
    }

    @Override
    public Set<String> getAvailablePlugins(ClockworkLoader loader) {
        scanIfNeeded(loader);
        return Set.copyOf(cache.keySet());
    }

    @Override
    public Set<Version> getAvailableVersions(ClockworkLoader loader, String pluginId) {
        scanIfNeeded(loader);
        final var refs = cache.getOrDefault(pluginId, List.of());
        return refs.stream().map(PluginReference::getVersion).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Optional<PluginReference> find(ClockworkLoader loader, String pluginId, Version version) {
        scanIfNeeded(loader);
        final List<PluginReference> refs = cache.getOrDefault(pluginId, List.of());
        return refs.stream().filter(ref -> ref.getVersion().equals(version)).findFirst();
    }

    protected void scanIfNeeded(ClockworkLoader loader) {
        if (cache != null) return;
        final var readers = readerNames == null
                ? loader.getFeatures().getAll(PluginReader.class)
                : loader.getFeatures().getAll(PluginReader.class, readerNames);
        cache = scan(loader, readers).stream().collect(Collectors.groupingBy(PluginReference::getId));
    }

    protected abstract Set<PluginReference> scan(ClockworkLoader loader, Collection<PluginReader> readers);

}
