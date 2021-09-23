package dev.m00nl1ght.clockwork.utils.config.impl;

import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;

import java.util.*;

public class ConfigImpl implements Config {

    protected final Map<String, Object> map;
    protected final ConfigSpec spec;

    protected ConfigImpl(ConfigSpec spec) {
        this.spec = spec;
        this.map = new HashMap<>();
    }

    public ConfigImpl(ConfigImpl other, ConfigSpec spec) {
        this.map = deepCopy(other.map);
        this.spec = spec;
    }

    protected Map<String, Object> deepCopy(Map<String, Object> src) {
        final var dest = new HashMap<String, Object>(src.size());
        src.forEach((k, v) -> {
            if (v instanceof Config) {
                dest.put(k, deepCopy((Config) v));
            } else if (v instanceof Config[]) {
                dest.put(k, Arrays.stream((Config[]) v).map(this::deepCopy).toArray(Config[]::new));
            } else if (v instanceof String[]) {
                dest.put(k, ((String[]) v).clone());
            } else {
                dest.put(k, v);
            }
        });
        return dest;
    }

    protected Config deepCopy(Config src) {
        return src.copy();
    }

    @Override
    public Set<String> getKeys() {
        return Set.copyOf(map.keySet());
    }

    @Override
    public String getString(String key) {
        final var val = map.get(key);
        return val instanceof String ? (String) val : null;
    }

    @Override
    public Config getSubconfig(String key) {
        final var val = map.get(key);
        return val instanceof Config ? (Config) val : null;
    }

    @Override
    public List<String> getStrings(String key) {
        final var val = map.get(key);
        return val instanceof String[] ? List.of((String[]) val) : null;
    }

    @Override
    public List<Config> getSubconfigs(String key) {
        final var val = map.get(key);
        return val instanceof Config[] ? List.of((Config[]) val) : null;
    }

    @Override
    public Config copy() {
        return this;
    }

    @Override
    public ModifiableConfig modifiableCopy(ConfigSpec spec) {
        return new ModifiableConfigImpl(this, spec);
    }

    @Override
    public ConfigSpec getSpec() {
        return spec;
    }

    @Override
    public String toString() {
        return "Config";
    }

}
