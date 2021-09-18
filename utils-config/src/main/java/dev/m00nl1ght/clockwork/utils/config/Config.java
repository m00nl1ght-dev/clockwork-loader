package dev.m00nl1ght.clockwork.utils.config;

import dev.m00nl1ght.clockwork.utils.config.impl.AttributesWrapper;
import dev.m00nl1ght.clockwork.utils.config.impl.EmptyConfig;
import dev.m00nl1ght.clockwork.utils.config.impl.ModifiableConfigImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.jar.Attributes;

public interface Config {

    Config EMPTY = EmptyConfig.INSTANCE;

    static ModifiableConfig newConfig() {
        return new ModifiableConfigImpl();
    }

    static Config fromAttributes(Attributes attributes) {
        return new AttributesWrapper(attributes);
    }

    static Config fromAttributes(Attributes attributes, String keyPrefix) {
        return new AttributesWrapper(attributes, keyPrefix);
    }

    Set<String> getKeys();

    String getOrNull(String key);

    Config getSubconfigOrNull(String key);

    List<String> getListOrNull(String key);

    List<? extends Config> getSubconfigListOrNull(String key);

    default String get(String key) {
        final var value = getOrNull(key);
        if (value == null) throw new RuntimeException("Missing value " + key + " in config " + this);
        return value;
    }

    default Optional<String> getOptional(String key) {
        return Optional.ofNullable(getOrNull(key));
    }

    default String getOrDefault(String key, String defaultValue) {
        final var value = getOrNull(key);
        return value == null ? defaultValue : value;
    }

    default Config getSubconfig(String key) {
        final var value = getSubconfigOrNull(key);
        if (value == null) throw new RuntimeException("Missing subconfig " + key + " in config " + this);
        return value;
    }

    default Optional<? extends Config> getOptionalSubconfig(String key) {
        return Optional.ofNullable(getSubconfigOrNull(key));
    }

    default Config getSubconfigOrEmpty(String key) {
        final var value = getSubconfigOrNull(key);
        return value == null ? Config.EMPTY : value;
    }

    default Config getSubconfigOrDefault(String key, Config defaultValue) {
        final var value = getSubconfigOrNull(key);
        return value == null ? defaultValue : value;
    }

    default List<String> getList(String key) {
        final var value = getListOrNull(key);
        if (value == null) throw new RuntimeException("Missing list " + key + " in config " + this);
        return value;
    }

    default Optional<List<String>> getOptionalList(String key) {
        return Optional.ofNullable(getListOrNull(key));
    }

    default List<String> getListOrEmpty(String key) {
        final var value = getListOrNull(key);
        return value == null ? List.of() : value;
    }

    default List<String> getListOrSingletonOrEmpty(String key) {
        final var list = getListOrNull(key);
        if (list != null) return list;
        final var value = getOrNull(key);
        if (value != null) return List.of(value);
        return List.of();
    }

    default List<? extends Config> getSubconfigList(String key) {
        final var value = getSubconfigListOrNull(key);
        if (value == null) throw new RuntimeException("Missing list " + key + " in config " + this);
        return value;
    }

    default Optional<List<? extends Config>> getOptionalSubconfigList(String key) {
        return Optional.ofNullable(getSubconfigListOrNull(key));
    }

    default List<? extends Config> getSubconfigListOrEmpty(String key) {
        final var value = getSubconfigListOrNull(key);
        return value == null ? List.of() : value;
    }

    default List<? extends Config> getSubconfigListOrSingletonOrEmpty(String key) {
        final var list = getSubconfigListOrNull(key);
        if (list != null) return list;
        final var value = getSubconfigOrNull(key);
        if (value != null) return List.of(value);
        return List.of();
    }

    default int getInt(String key) {
        return asInt(key, get(key));
    }

    default int getIntOrDefault(String key, int defaultValue) {
        final var value = getOrNull(key);
        return value == null ? defaultValue : asInt(key, value);
    }

    private int asInt(String key, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Inavalid value " + key + " in config " + this + " (" + value + " is not an Integer)");
        }
    }

    default float getFloat(String key) {
        return asInt(key, get(key));
    }

    default float getFloatOrDefault(String key, float defaultValue) {
        final var value = getOrNull(key);
        return value == null ? defaultValue : asInt(key, value);
    }

    private float asFloat(String key, String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Inavalid value " + key + " in config " + this + " (" + value + " is not a Float)");
        }
    }

    default boolean getBoolean(String key) {
        return asBoolean(key, get(key));
    }

    default boolean getBooleanOrDefault(String key, boolean defaultValue) {
        final var value = getOrNull(key);
        return value == null ? defaultValue : asBoolean(key, value);
    }

    default boolean getBooleanOrFalse(String key) {
        return getBooleanOrDefault(key, false);
    }

    private boolean asBoolean(String key, String value) {
        try {
            return Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Inavalid value " + key + " in config " + this + " (" + value + " is not a Boolean)");
        }
    }

    default <E extends Enum<E>> E getEnum(String key, Class<E> enumClass) {
        return asEnum(key, get(key), enumClass);
    }

    default <E extends Enum<E>> E getEnumOrDefault(String key, Class<E> enumClass, E defaultValue) {
        final var value = getOrNull(key);
        return value == null ? defaultValue : asEnum(key, value, enumClass);
    }

    private <E extends Enum<E>> E asEnum(String key, String value, Class<E> enumClass) {
        final var found = Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.name().equalsIgnoreCase(value)).findAny();
        return found.orElseThrow(() -> new RuntimeException("Inavalid value " + key + " in config " + this + " (enum constant " + value + " does not exist)"));
    }

    Config copy();

    ModifiableConfig modifiableCopy();

    default Config asReadonly() {
        return this;
    }

    default StrictConfig asStrict() {
        return new StrictConfig(this);
    }

}
