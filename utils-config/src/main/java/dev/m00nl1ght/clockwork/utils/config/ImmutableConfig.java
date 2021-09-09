package dev.m00nl1ght.clockwork.utils.config;

import java.util.*;

public class ImmutableConfig implements Config {

    private final Map<String, Object> map;

    protected ImmutableConfig(Builder builder) {
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
    public Config immutable() {
        return this;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected final Map<String, Object> map = new HashMap<>();

        private Builder() {}

        public ImmutableConfig build() {
            return new ImmutableConfig(this);
        }

        public Builder putString(String key, Object value) {
            if (value == null) return this;
            map.put(key, value.toString());
            return this;
        }

        public Builder putSubconfig(String key, Config value) {
            if (value == null) return this;
            map.put(key, value.immutable());
            return this;
        }

        public Builder putStrings(String key, Collection<String> value) {
            if (value == null) return this;
            map.put(key, value.toArray(String[]::new));
            return this;
        }

        public Builder putSubconfigs(String key, Collection<? extends Config> value) {
            if (value == null) return this;
            map.put(key, value.stream().map(Config::immutable).toArray(Config[]::new));
            return this;
        }

    }

}
