package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.core.ComponentDescriptor;
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
    public Collection<PluginDefinition> find(ComponentDescriptor target) {
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

    private void accept(PluginDefinition def) {
        final var prev = cache.putIfAbsent(def.getId(), def);
        if (prev != null) throw PluginLoadingException.pluginDuplicate(this, def, prev);
    }

    protected abstract void scan(Consumer<PluginDefinition> pluginConsumer);

}
