package dev.m00nl1ght.clockwork.utils.config.impl;

import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public String getString(String key) {
        return config.getString(key);
    }

    @Override
    public Config getSubconfig(String key) {
        return config.getSubconfig(key);
    }

    @Override
    public List<String> getStrings(String key) {
        return config.getStrings(key);
    }

    @Override
    public List<? extends Config> getSubconfigs(String key) {
        return config.getSubconfigs(key);
    }

    @Override
    public Config copy() {
        return config.copy();
    }

    @Override
    public @NotNull ModifiableConfig modifiableCopy(@Nullable ConfigSpec spec) {
        return config.modifiableCopy(spec);
    }

    @Override
    public String toString() {
        return config.toString();
    }

}
