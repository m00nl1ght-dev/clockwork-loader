package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.util.Arguments;

import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractCachedLocator implements PluginLocator {

    protected final Set<PluginReader> readers;
    protected final LocatorConfig config;

    private Map<String, PluginReference> cache;

    protected AbstractCachedLocator(LocatorConfig config, Set<PluginReader> readers) {
        this.config = Arguments.notNull(config, "config");
        this.readers = Set.copyOf(Arguments.notNull(readers, "readers"));
    }

    @Override
    public Collection<PluginReference> findAll() {
        scanIfNeeded();
        return Collections.unmodifiableCollection(cache.values());
    }

    @Override
    public Collection<PluginReference> find(DependencyDescriptor target) {
        scanIfNeeded();
        final var ret = cache.get(target.getPlugin());
        if (ret != null && target.acceptsVersion(ret.getVersion())) {
            return Collections.singleton(ret);
        } else {
            return Collections.emptySet();
        }
    }

    private void scanIfNeeded() {
        if (cache == null) {
            cache = new HashMap<>();
            scan(this::accept);
        }
    }

    private void accept(PluginReference def) {
        final var prev = cache.putIfAbsent(def.getId(), def);
        if (prev != null) throw PluginLoadingException.pluginDuplicate(def.getDescriptor(), prev.getDescriptor());
    }

    protected abstract void scan(Consumer<PluginReference> pluginConsumer);

    @Override
    public String toString() {
        return config.getLocator();
    }

}
