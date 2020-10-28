package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.core.LoadingContext;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.version.Version;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractPluginFinder implements PluginFinder {

    protected final PluginFinderConfig config;

    private Map<String, Optional<PluginReference>> cache;

    protected AbstractPluginFinder(PluginFinderConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public Set<String> getAvailablePlugins(LoadingContext context) {
        scanIfNeeded(context);
        return Set.copyOf(cache.keySet());
    }

    @Override
    public Set<Version> getAvailableVersions(LoadingContext context, String pluginId) {
        scanIfNeeded(context);
        final var ref = cache.getOrDefault(pluginId, Optional.empty());
        return ref.isEmpty() ? Collections.emptySet() : Collections.singleton(ref.get().getVersion());
    }

    @Override
    public Optional<PluginReference> find(LoadingContext context, String pluginId, Version version) {
        scanIfNeeded(context);
        final var ref = cache.getOrDefault(pluginId, Optional.empty());
        return ref.isPresent() && ref.get().getVersion().equals(version) ? ref : Optional.empty();
    }

    protected void scanIfNeeded(LoadingContext context) {
        if (cache != null) return;
        final var readers = config.getReaders() == null ? context.getReaders() : config.getReaders().stream()
                .map(context::getReader)
                .collect(Collectors.toUnmodifiableList());
        cache = scan(context, readers).stream()
                .collect(Collectors.groupingBy(PluginReference::getId,
                        Collectors.reducing(this::onDuplicate)));
    }

    protected abstract Set<PluginReference> scan(LoadingContext context, Collection<PluginReader> readers);

    protected PluginReference onDuplicate(PluginReference a, PluginReference b) {
        throw FormatUtil.rtExc("[] found multiple versions of the same plugin: [] and []", this, a, b);
    }

    @Override
    public String toString() {
        return config.getType() + "[" + config.getName() +  "]";
    }

}
