package dev.m00nl1ght.clockwork.utils.config.impl;

import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;

import java.util.*;
import java.util.stream.Collectors;

public class ModifiableConfigImpl extends ConfigImpl implements ModifiableConfig {

    public ModifiableConfigImpl(ConfigSpec spec) {
        super(spec);
    }

    public ModifiableConfigImpl(ConfigImpl other, ConfigSpec spec) {
        super(other, spec);
    }

    @Override
    protected Config deepCopy(Config src) {
        return src.modifiableCopy();
    }

    @Override
    public ModifiableConfig getModifiableSubconfig(String key) {
        final var config = getSubconfig(key);
        if (config == null || config instanceof ModifiableConfig) return (ModifiableConfig) config;
        final var mConfig = config.modifiableCopy();
        map.put(key, mConfig);
        return mConfig;
    }

    @Override
    public List<ModifiableConfig> getModifiableSubconfigs(String key) {
        final var list = getSubconfigs(key);
        if (list == null) return null;
        final var mList = list.stream().map(c -> c instanceof ModifiableConfig
                ? (ModifiableConfig) c : c.modifiableCopy())
                .collect(Collectors.toList());
        map.put(key, mList.toArray(Config[]::new));
        return mList;
    }

    @Override
    public ModifiableConfig putString(String key, Object value) {
        map.put(Objects.requireNonNull(key), value == null ? null : value.toString());
        return this;
    }

    @Override
    public ModifiableConfig putSubconfig(String key, Config value) {
        map.put(Objects.requireNonNull(key), value);
        return this;
    }

    @Override
    public ModifiableConfig putStrings(String key, Collection<String> value) {
        map.put(Objects.requireNonNull(key), value == null ? null : value.toArray(String[]::new));
        return this;
    }

    @Override
    public ModifiableConfig putSubconfigs(String key, Collection<? extends Config> value) {
        map.put(Objects.requireNonNull(key), value == null ? null : value.toArray(Config[]::new));
        return this;
    }

    @Override
    public Config copy() {
        return new ConfigImpl(this, spec);
    }

    @Override
    public String toString() {
        return "ModifiableConfig";
    }

}
