package dev.m00nl1ght.clockwork.utils.config.impl;

import dev.m00nl1ght.clockwork.utils.config.Config;

public class CachedConfig extends ReadonlyWrapper {

    public CachedConfig(Config config) {
        super(config);
    }

    @Override
    public <T> T getOrNull(String key, Type<T> valueType) {
        // TODO implement caching
        final var value = super.getOrNull(key, valueType);
        return value;
    }

}
