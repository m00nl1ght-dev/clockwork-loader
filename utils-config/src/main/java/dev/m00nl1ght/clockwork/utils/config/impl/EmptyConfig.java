package dev.m00nl1ght.clockwork.utils.config.impl;

import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public String getString(String key) {
        return null;
    }

    @Override
    public Config getSubconfig(String key) {
        return null;
    }

    @Override
    public List<String> getStrings(String key) {
        return null;
    }

    @Override
    public List<Config> getSubconfigs(String key) {
        return null;
    }

    @Override
    public Config copy() {
        return INSTANCE;
    }

    @Override
    public @NotNull ModifiableConfig modifiableCopy(@Nullable ConfigSpec spec) {
        return new ModifiableConfigImpl(spec);
    }

    @Override
    public String toString() {
        return "EmptyConfig";
    }

}
