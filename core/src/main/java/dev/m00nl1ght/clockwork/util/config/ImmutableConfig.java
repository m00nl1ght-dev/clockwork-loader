package dev.m00nl1ght.clockwork.util.config;

import dev.m00nl1ght.clockwork.util.Arguments;

import java.util.*;

public class ImmutableConfig implements Config {

    private final String configName;
    private final Map<String, Object> map;

    protected ImmutableConfig(Builder builder) {
        this.configName = builder.configName;
        this.map = Map.copyOf(builder.map);
    }

    @Override
    public Set<String> getKeys() {
        return Set.copyOf(map.keySet());
    }

    @Override
    public String getOrNull(String key) {
        final var val = map.get(key);
        return val instanceof String ? (String) val : null;
    }

    @Override
    public Config getSubconfigOrNull(String key) {
        final var val = map.get(key);
        return val instanceof Config ? (Config) val : null;
    }

    @Override
    public List<String> getListOrNull(String key) {
        final var val = map.get(key);
        return val instanceof String[] ? List.of((String[]) val) : null;
    }

    @Override
    public List<Config> getSubconfigListOrNull(String key) {
        final var val = map.get(key);
        return val instanceof Config[] ? List.of((Config[]) val) : null;
    }

    @Override
    public String toString() {
        return configName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected final Map<String, Object> map = new HashMap<>();
        protected String configName = "ImmutableConfig";

        protected Builder() {}

        public ImmutableConfig build() {
            return new ImmutableConfig(this);
        }

        public Builder withName(String configName) {
            this.configName = Arguments.notNull(configName, "configName");
            return this;
        }

        public Builder put(String key, Object value) {
            Arguments.notNull(value, "value");
            map.put(key, value.toString());
            return this;
        }

        public Builder put(String key, Config value) {
            Arguments.notNull(value, "value");
            map.put(key, value);
            return this;
        }

        public Builder putList(String key, Collection<String> value) {
            Arguments.notNull(value, "value");
            map.put(key, value.toArray(String[]::new));
            return this;
        }

        public Builder putSubconfigs(String key, Collection<? extends Config> value) {
            Arguments.notNull(value, "value");
            map.put(key, value.toArray(Config[]::new));
            return this;
        }

    }

}
