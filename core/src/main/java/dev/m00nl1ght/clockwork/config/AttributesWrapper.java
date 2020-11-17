package dev.m00nl1ght.clockwork.config;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.stream.Collectors;

public class AttributesWrapper implements Config {

    protected final Attributes attributes;
    protected final String keyPrefix;

    public AttributesWrapper(Attributes attributes) {
        this(attributes, "");
    }

    public AttributesWrapper(Attributes attributes, String keyPrefix) {
        this.attributes = Objects.requireNonNull(attributes);
        this.keyPrefix = Objects.requireNonNull(keyPrefix);
    }

    @Override
    public Set<String> getKeys() {
        if (keyPrefix.isEmpty()) {
            return attributes.keySet().stream()
                    .map(Object::toString)
                    .collect(Collectors.toUnmodifiableSet());
        } else {
            return attributes.keySet().stream()
                    .map(Object::toString)
                    .filter(k -> k.startsWith(keyPrefix))
                    .map(k -> k.substring(keyPrefix.length()))
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    @Override
    public String getOrNull(String key) {
        final var raw = attributes.getValue(keyPrefix + key);
        if (raw == null) return null;
        final var val = raw.strip();
        if (isList(val) || isConfig(val)) return null;
        if (isQuoted(val)) return raw.substring(1, raw.length() - 1);
        return raw;
    }

    @Override
    public Config getSubconfigOrNull(String key) {
        final var raw = attributes.getValue(keyPrefix + key);
        if (raw == null || !isConfig(raw.strip())) return null;
        return SimpleDataParser.parse(SimpleDataParser.DEFAULT_CONFIG, raw);
    }

    @Override
    public List<String> getListOrNull(String key) {
        final var raw = attributes.getValue(keyPrefix + key);
        if (raw == null || !isList(raw.strip())) return null;
        return SimpleDataParser.parse(SimpleDataParser.DEFAULT_STRING_LIST, raw);
    }

    @Override
    public List<Config> getSubconfigListOrNull(String key) {
        final var raw = attributes.getValue(keyPrefix + key);
        if (raw == null || !isList(raw.strip())) return null;
        return SimpleDataParser.parse(SimpleDataParser.DEFAULT_CONFIG_LIST, raw);
    }

    @Override
    public Config immutable() {
        final var builder = ImmutableConfig.builder();

        for (final var entry : attributes.entrySet()) {
            final var rawKey = entry.getKey().toString().strip();
            if (!rawKey.startsWith(keyPrefix)) continue;
            final var key = rawKey.substring(keyPrefix.length());
            final var value = entry.getValue().toString().strip();
            if (isConfig(value)) {
                final var config = SimpleDataParser.parse(SimpleDataParser.DEFAULT_CONFIG, value);
                if (config != null) {
                    builder.putSubconfig(key, config);
                }
            } else if (isList(value)) {
                final var stringList = SimpleDataParser.parse(SimpleDataParser.DEFAULT_STRING_LIST, value);
                if (stringList != null) {
                    builder.putStrings(key, stringList);
                } else {
                    final var configList = SimpleDataParser.parse(SimpleDataParser.DEFAULT_CONFIG_LIST, value);
                    if (configList != null) {
                        builder.putSubconfigs(key, configList);
                    }
                }
            } else if (isQuoted(value)) {
                builder.putString(key, value.substring(1, value.length() - 1));
            } else {
                builder.putString(key, value);
            }
        }

        return builder.build();
    }

    protected boolean isList(String val) {
        return val.charAt(0) == '[' &&
                val.charAt(val.length() - 1) == ']';
    }

    protected boolean isConfig(String val) {
        return val.charAt(0) == '{' &&
                val.charAt(val.length() - 1) == '}';
    }

    protected boolean isQuoted(String val) {
        return val.charAt(0) == '"' &&
                val.charAt(val.length() - 1) == '"';
    }

}
