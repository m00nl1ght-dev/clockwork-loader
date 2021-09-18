package dev.m00nl1ght.clockwork.utils.config.impl;

import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;

import java.util.List;
import java.util.Set;

public class EmptyConfig implements Config {

    public static final Config INSTANCE = new EmptyConfig();

    private EmptyConfig() {}

    @Override
    public Set<String> getKeys() {
        return Set.of();
    }

    @Override
    public String getOrNull(String key) {
        return null;
    }

    @Override
    public Config getSubconfigOrNull(String key) {
        return null;
    }

    @Override
    public List<String> getListOrNull(String key) {
        return null;
    }

    @Override
    public List<Config> getSubconfigListOrNull(String key) {
        return null;
    }

    @Override
    public Config copy() {
        return INSTANCE;
    }

    @Override
    public ModifiableConfig modifiableCopy() {
        return new ModifiableConfigImpl();
    }

}
