package dev.m00nl1ght.clockwork.utils.config.impl;

import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ConfigException;
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
    protected Config deepCopy(Config src, ConfigSpec spec) {
        return src.modifiableCopy(spec);
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
    public ModifiableConfig putString(String key, String value) {
        map.put(Objects.requireNonNull(key), value);
        checkSpec(key);
        return this;
    }

    @Override
    public ModifiableConfig putSubconfig(String key, Config value) {
        map.put(Objects.requireNonNull(key), value);
        checkSpec(key);
        return this;
    }

    @Override
    public ModifiableConfig putStrings(String key, Collection<String> value) {
        map.put(Objects.requireNonNull(key), value == null ? null : value.toArray(String[]::new));
        checkSpec(key);
        return this;
    }

    @Override
    public ModifiableConfig putSubconfigs(String key, Collection<? extends Config> value) {
        map.put(Objects.requireNonNull(key), value == null ? null : value.toArray(Config[]::new));
        checkSpec(key);
        return this;
    }

    private void checkSpec(String key) {
        if (spec == null) return;
        final var entry = spec.getEntry(key);
        if (entry != null) {
            final var exc = entry.getType().verify(this, key);
            if (exc != null) throw exc;
        } else if (!spec.areAdditionalEntriesAllowed()) {
            throw new ConfigException(this, "Unknown entry " + key + " in " + this + " but not in spec " + spec);
        }
    }

    @Override
    public Config copy(ConfigSpec spec) {
        return new ConfigImpl(this, spec);
    }

    @Override
    public ModifiableConfig modifiableCopy(ConfigSpec spec) {
        return new ModifiableConfigImpl(this, spec);
    }

    @Override
    public String toString() {
        return "ModifiableConfig";
    }

}
