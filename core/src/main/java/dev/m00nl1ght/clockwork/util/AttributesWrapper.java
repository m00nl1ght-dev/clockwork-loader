package dev.m00nl1ght.clockwork.util;

import dev.m00nl1ght.clockwork.util.config.Config;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.stream.Collectors;

public class AttributesWrapper implements Config {

    private final String configName;
    private final Attributes attributes;

    public AttributesWrapper(Attributes attributes) {
        this("Manifest-Attributes", attributes);
    }

    public AttributesWrapper(String configName, Attributes attributes) {
        this.configName = configName;
        this.attributes = attributes;
    }

    @Override
    public Set<String> getKeys() {
        return attributes.keySet().stream().map(Object::toString).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getOrNull(String key) {
        return attributes.getValue(key);
    }

    @Override
    public Config getSubconfigOrNull(String key) {
        return null;
    }

    @Override
    public List<String> getListOrNull(String key) {
        final var val = attributes.getValue(key);
        if (val == null) return null;
        return Arrays.stream(val.split(" "))
                .map(String::trim)
                .filter(str -> !str.isEmpty())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Config> getSubconfigListOrNull(String key) {
        return null;
    }

    @Override
    public String toString() {
        return configName;
    }

}
