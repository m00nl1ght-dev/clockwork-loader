package dev.m00nl1ght.clockwork.utils.config.impl;

import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ReadonlyWrapper implements Config {

    protected final Config config;

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
    public List<Config> getSubconfigs(String key) {
        return config.getSubconfigs(key);
    }

    @Override
    public <T> @Nullable T get(ConfigSpec.@NotNull Entry<T> entry) {
        return config.get(entry);
    }

    @Override
    public @Nullable Object getObject(@NotNull String key) {
        return config.getObject(key);
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull Object> toMap() {
        return config.toMap();
    }

    @Override
    public Config copy(@Nullable ConfigSpec spec) {
        return config.copy(spec);
    }

    @Override
    public @NotNull ModifiableConfig modifiableCopy(@Nullable ConfigSpec spec) {
        return config.modifiableCopy(spec);
    }

    @Override
    public @Nullable ConfigSpec getSpec() {
        return config.getSpec();
    }

    @Override
    public String toString() {
        return config.toString();
    }

}
