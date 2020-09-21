package dev.m00nl1ght.clockwork.util;

import java.util.HashMap;
import java.util.Map;

public class ImmutableConfig implements Config {

    private final Map<String, String> entries;

    public ImmutableConfig() {
        this(Map.of());
    }

    protected ImmutableConfig(Builder builder) {
        this(builder.entries);
    }

    public ImmutableConfig(Map<String, String> entries) {
        this.entries = Arguments.snapshot(entries, "entries");
    }

    @Override
    public Map<String, String> getEntries() {
        return entries;
    }

    @Override
    public String getOrNull(String key) {
        return entries.get(key);
    }

    public static class Builder {

        protected final Map<String, String> entries = new HashMap<>();

        public ImmutableConfig build() {
            return new ImmutableConfig(this);
        }

        public void set(String key, String value) {
            entries.put(key, value);
        }

        public Map<String, String> getEntries() {
            return Map.copyOf(entries);
        }

    }

}
