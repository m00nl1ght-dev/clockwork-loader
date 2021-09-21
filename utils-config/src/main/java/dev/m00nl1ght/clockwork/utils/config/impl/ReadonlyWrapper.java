package dev.m00nl1ght.clockwork.utils.config.impl;

import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ReadonlyWrapper implements Config {

    private final Config config;

    public ReadonlyWrapper(Config config) {
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public Set<String> getKeys() {
        return config.getKeys();
    }

    @Override
    public String getOrNull(String key) {
        return config.getOrNull(key);
    }

    @Override
    public Config getSubconfigOrNull(String key) {
        return config.getSubconfigOrNull(key);
    }

    @Override
    public List<String> getListOrNull(String key) {
        return config.getListOrNull(key);
    }

    @Override
    public List<? extends Config> getSubconfigListOrNull(String key) {
        return config.getSubconfigListOrNull(key);
    }

    @Override
    public Config copy() {
        return config.copy();
    }

    @Override
    public ModifiableConfig modifiableCopy() {
        return config.modifiableCopy();
    }

    @Override
    public String toString() {
        return config.toString();
    }

}
