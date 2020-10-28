package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.core.LoadingContext;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.version.Version;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractIndexedPluginFinder implements PluginFinder {

    protected final PluginFinderConfig config;

    private Map<String, Map<Version, PluginReference>> index;

    protected AbstractIndexedPluginFinder(PluginFinderConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public Set<String> getAvailablePlugins(LoadingContext context) {
        if (index != null) Set.copyOf(index.keySet());
        index = new HashMap<>();
        indexPlugins(context).forEach(p -> index.put(p, null));
        return Set.copyOf(index.keySet());
    }

    @Override
    public Set<Version> getAvailableVersions(LoadingContext context, String pluginId) {
        if (!getAvailablePlugins(context).contains(pluginId)) return Collections.emptySet();
        final var fromIndex = index.get(pluginId);
        if (fromIndex != null) return Set.copyOf(fromIndex.keySet());
        final Map<Version, PluginReference> versions = indexVersions(context, pluginId).stream()
                .collect(Collectors.toMap(Function.identity(), p -> null, (a, b) -> b, HashMap::new));
        index.put(pluginId, versions);
        return Set.copyOf(versions.keySet());
    }

    @Override
    public Optional<PluginReference> find(LoadingContext context, String pluginId, Version version) {
        if (!getAvailableVersions(context, pluginId).contains(version)) return Optional.empty();
        final var versionIndex = index.get(pluginId);
        final var fromIndex = versionIndex.get(version);
        if (fromIndex != null) return Optional.of(fromIndex);
        final var readers = config.getReaders() == null ? context.getReaders() : config.getReaders().stream()
                .map(context::getReader)
                .collect(Collectors.toUnmodifiableList());
        final var found = find(context, readers, pluginId, version);
        if (found.isEmpty()) return found;
        versionIndex.put(version, found.get());
        return found;
    }

    protected abstract Set<String> indexPlugins(LoadingContext context);

    protected abstract Set<Version> indexVersions(LoadingContext context, String pluginId);

    protected abstract Optional<PluginReference> find(LoadingContext context, Collection<PluginReader> readers, String pluginId, Version version);

    @Override
    public String toString() {
        return config.getType() + "[" + config.getName() +  "]";
    }

}
