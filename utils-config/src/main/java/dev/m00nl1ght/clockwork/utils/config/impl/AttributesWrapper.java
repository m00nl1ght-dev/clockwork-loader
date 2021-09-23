package dev.m00nl1ght.clockwork.utils.config.impl;

import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;
import dev.m00nl1ght.clockwork.utils.config.SimpleDataParser;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.stream.Collectors;

public class AttributesWrapper implements Config {

    protected final Attributes attributes;
    protected final SimpleDataParser.Format dataFormat;
    protected final SimpleDataParser.StringSegment stringFormat;
    protected final SimpleDataParser.ConfigSegment configFormat;
    protected final SimpleDataParser.StringListSegment listFormat;
    protected final SimpleDataParser.ConfigListSegment configListFormat;
    protected final String keyPrefix;

    public AttributesWrapper(Attributes attributes, SimpleDataParser.Format dataFormat, String keyPrefix) {
        this.attributes = Objects.requireNonNull(attributes);
        this.dataFormat = Objects.requireNonNull(dataFormat);
        this.stringFormat = dataFormat.getSegment(SimpleDataParser.StringSegment.class).orElseThrow();
        this.configFormat = dataFormat.getSegment(SimpleDataParser.ConfigSegment.class).orElseThrow();
        this.listFormat = dataFormat.getSegment(SimpleDataParser.StringListSegment.class).orElseThrow();
        this.configListFormat = dataFormat.getSegment(SimpleDataParser.ConfigListSegment.class).orElseThrow();
        this.keyPrefix = Objects.requireNonNull(keyPrefix);
    }

    @Override
    public Set<String> getKeys() {
        return keyPrefix.isEmpty()
                ? attributes.keySet().stream()
                    .map(Object::toString)
                    .collect(Collectors.toUnmodifiableSet())
                : attributes.keySet().stream()
                    .map(Object::toString)
                    .filter(k -> k.startsWith(keyPrefix))
                    .map(k -> k.substring(keyPrefix.length()))
                    .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getString(String key) {
        final var raw = attributes.getValue(keyPrefix + key);
        return raw == null ? null : SimpleDataParser.parse(dataFormat, stringFormat, raw);
    }

    @Override
    public Config getSubconfig(String key) {
        final var raw = attributes.getValue(keyPrefix + key);
        return raw == null ? null : SimpleDataParser.parse(dataFormat, configFormat, raw);
    }

    @Override
    public List<String> getStrings(String key) {
        final var raw = attributes.getValue(keyPrefix + key);
        return raw == null ? null : SimpleDataParser.parse(dataFormat, listFormat, raw);
    }

    @Override
    public List<? extends Config> getSubconfigs(String key) {
        final var raw = attributes.getValue(keyPrefix + key);
        return raw == null ? null : SimpleDataParser.parse(dataFormat, configListFormat, raw);
    }

    @Override
    public Config copy() {
        return modifiableCopy().copy();
    }

    @Override
    public ModifiableConfig modifiableCopy(@Nullable ConfigSpec spec) {
        final var config = new ModifiableConfigImpl(spec);

        for (final var entry : attributes.entrySet()) {
            final var rawKey = entry.getKey().toString().strip();
            if (!rawKey.startsWith(keyPrefix)) continue;
            final var key = rawKey.substring(keyPrefix.length());
            final var value = entry.getValue().toString().strip();

            final var subconfig = SimpleDataParser.parse(dataFormat, configFormat, value);
            if (subconfig != null) {
                config.putSubconfig(key, subconfig);
                continue;
            }

            final var stringList = SimpleDataParser.parse(dataFormat, listFormat, value);
            if (stringList != null) {
                config.putStrings(key, stringList);
                continue;
            }

            final var configList = SimpleDataParser.parse(dataFormat, configListFormat, value);
            if (configList != null) {
                config.putSubconfigs(key, configList);
                continue;
            }

            final var str = SimpleDataParser.parse(dataFormat, stringFormat, value);
            if (str != null) {
                config.putString(key, str);
            }
        }

        return config;
    }

    @Override
    public String toString() {
        return "Attributes";
    }

}
