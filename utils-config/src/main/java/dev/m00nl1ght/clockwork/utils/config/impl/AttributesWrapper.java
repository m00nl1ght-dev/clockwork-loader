package dev.m00nl1ght.clockwork.utils.config.impl;

import dev.m00nl1ght.clockwork.utils.config.SimpleDataParser;

import java.util.Objects;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.stream.Collectors;

public class AttributesWrapper extends AbstractSDPConfig {

    protected final Attributes attributes;
    protected final String keyPrefix;

    public AttributesWrapper(Attributes attributes, SimpleDataParser.Format dataFormat, String keyPrefix) {
        super(dataFormat);
        this.attributes = Objects.requireNonNull(attributes);
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
    protected String getRaw(String key) {
        return attributes.getValue(keyPrefix + key);
    }

    @Override
    public String toString() {
        return "Attributes";
    }

}
