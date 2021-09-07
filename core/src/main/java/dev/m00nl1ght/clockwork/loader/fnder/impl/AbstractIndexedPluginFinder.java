package dev.m00nl1ght.clockwork.loader.fnder.impl;

import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.utils.version.Version;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractIndexedPluginFinder implements PluginFinder {

    private Map<String, Map<Version, PluginReference>> index;

    protected final String name;
    protected final List<String> readerNames;
    protected final boolean wildcard;

    protected AbstractIndexedPluginFinder(ClockworkLoader loader, Config config) {
        this.name = config.get("name");
        this.readerNames = config.getListOrNull("readers");
        this.wildcard = config.getBooleanOrDefault("wildcard", false);
    }

    @Override
    public Set<String> getAvailablePlugins(ClockworkLoader loader) {
        if (index != null) return Set.copyOf(index.keySet());
        index = new HashMap<>();
        indexPlugins(loader).forEach(p -> index.put(p, null));
        return Set.copyOf(index.keySet());
    }

    @Override
    public Set<Version> getAvailableVersions(ClockworkLoader loader, String pluginId) {
        if (!getAvailablePlugins(loader).contains(pluginId)) return Collections.emptySet();
        final var fromIndex = index.get(pluginId);
        if (fromIndex != null) return Set.copyOf(fromIndex.keySet());
        final Map<Version, PluginReference> versions = indexVersions(loader, pluginId).stream()
                .collect(Collectors.toMap(Function.identity(), p -> null, (a, b) -> b, HashMap::new));
        index.put(pluginId, versions);
        return Set.copyOf(versions.keySet());
    }

    @Override
    public Optional<PluginReference> find(ClockworkLoader loader, String pluginId, Version version) {
        if (!getAvailableVersions(loader, pluginId).contains(version)) return Optional.empty();
        final var versionIndex = index.get(pluginId);
        final var fromIndex = versionIndex.get(version);
        if (fromIndex != null) return Optional.of(fromIndex);
        final var readers = readerNames == null
                ? loader.getFeatures().getAll(PluginReader.class)
                : loader.getFeatures().getAll(PluginReader.class, readerNames);
        final var found = find(loader, readers, pluginId, version);
        if (found.isEmpty()) return found;
        versionIndex.put(version, found.get());
        return found;
    }

    @Override
    public boolean isWildcard() {
        return wildcard;
    }

    protected abstract Set<String> indexPlugins(ClockworkLoader loader);

    protected abstract Set<Version> indexVersions(ClockworkLoader loader, String pluginId);

    protected abstract Optional<PluginReference> find(ClockworkLoader loader, Collection<PluginReader> readers, String pluginId, Version version);

}
