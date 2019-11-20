package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;

import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractCachedLocator implements PluginLocator {

    private Map<String, PluginDefinition> cache;

    @Override
    public Collection<PluginDefinition> findAll() {
        scanIfNeeded();
        return Collections.unmodifiableCollection(cache.values());
    }

    @Override
    public Optional<PluginDefinition> find(String plugin_id) {
        scanIfNeeded();
        return Optional.ofNullable(cache.get(plugin_id));
    }

    private void scanIfNeeded() {
        if (cache == null) {
            cache = new HashMap<>();
            scan(this::accept);
        }
    }

    private void accept(PluginDefinition def) {
        final var prev = cache.putIfAbsent(def.getId(), def);
        if (prev != null) throw PluginLoadingException.pluginDuplicate(this, def, prev);
    }

    protected abstract void scan(Consumer<PluginDefinition> pluginConsumer);

}
