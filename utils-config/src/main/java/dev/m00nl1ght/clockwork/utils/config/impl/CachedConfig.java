package dev.m00nl1ght.clockwork.utils.config.impl;

import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class CachedConfig extends ReadonlyWrapper {

    protected static final Object NULL_MARKER = new Object();

    protected final Object[] cache;

    public CachedConfig(Config config) {
        super(config);
        final var spec = config.getSpec();
        if (spec == null) throw new IllegalArgumentException("CachedConfig requires a spec");
        this.cache = new Object[spec.getEntries().size()];
    }

    @Override
    public <T> @Nullable T get(@NotNull Entry<T> entry) {
        final var cached = cache[entry.getSortIndex()];
        if (cached == null) {
            final var value = config.get(entry);
            cache[entry.getSortIndex()] = value == null ? NULL_MARKER : value;
            return value;
        } else {
            if (cached == NULL_MARKER) return null;
            @SuppressWarnings("unchecked")
            final var casted = (T) cached;
            return casted;
        }
    }

    public void clearCache() {
        Arrays.fill(cache, null);
    }

}
