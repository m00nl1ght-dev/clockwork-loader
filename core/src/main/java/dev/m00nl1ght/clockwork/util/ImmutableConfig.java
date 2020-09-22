package dev.m00nl1ght.clockwork.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.Attributes;

public class ImmutableConfig implements Config {

    private final Map<String, String> entries;
    private final String configName;

    public ImmutableConfig(String configName) {
        this(Map.of(), configName);
    }

    protected ImmutableConfig(Builder builder) {
        this(builder.entries, builder.configName);
    }

    public ImmutableConfig(Map<String, String> entries, String configName) {
        this.entries = Arguments.snapshot(entries, "entries");
        this.configName = Arguments.notNullOrBlank(configName, "configName");
    }

    @Override
    public Map<String, String> getEntries() {
        return entries;
    }

    @Override
    public String getOrNull(String key) {
        return entries.get(key);
    }

    @Override
    public String toString() {
        return configName;
    }

    public static ImmutableConfig fromAttributes(Attributes attributes) {
        return fromAttributes(attributes, "Manifest-Attributes");
    }

    public static ImmutableConfig fromAttributes(Attributes attributes, String configName) {
        final Map<String, String> map = new LinkedHashMap<>();
        attributes.forEach((k, v) -> map.put(k.toString(), v.toString()));
        return new ImmutableConfig(map, configName);
    }

    public static class Builder {

        protected final Map<String, String> entries = new HashMap<>();
        protected String configName = "ImmutableConfig";

        public ImmutableConfig build() {
            return new ImmutableConfig(this);
        }

        public void configName(String configName) {
            this.configName = configName;
        }

        public void set(String key, String value) {
            entries.put(key, value);
        }

        public Map<String, String> getEntries() {
            return Map.copyOf(entries);
        }

    }

}
