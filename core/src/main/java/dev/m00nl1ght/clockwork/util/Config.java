package dev.m00nl1ght.clockwork.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public interface Config {

    Map<String, String> getEntries();

    default String getOrNull(String key) {
        return getEntries().get(key);
    }

    default String get(String key) {
        final var value = getOrNull(key);
        if (value == null) throw FormatUtil.rtExc("Missing param [] in config []", key, this);
        return value;
    }

    default Optional<String> getOptional(String key) {
        return Optional.ofNullable(getOrNull(key));
    }

    default String getOrDefault(String key, String defaultValue) {
        final var value = getOrNull(key);
        return value == null ? defaultValue : value;
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
            throw FormatUtil.rtExc("Inavalid param [] in config [] (value [] is not an Integer)", key, this, value);
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
            throw FormatUtil.rtExc("Inavalid param [] in config [] (value [] is not a Float)", key, this, value);
        }
    }

    default boolean getBoolean(String key) {
        return asBoolean(key, get(key));
    }

    default boolean getBooleanOrDefault(String key, boolean defaultValue) {
        final var value = getOrNull(key);
        return value == null ? defaultValue : asBoolean(key, value);
    }

    private boolean asBoolean(String key, String value) {
        try {
            return Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            throw FormatUtil.rtExc("Inavalid param [] in config [] (value [] is not a Boolean)", key, this, value);
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
        return found.orElseThrow(() -> FormatUtil.rtExc("Inavalid param [] in config [] (enum constant [] does not exist)", key, this, value));
    }

}
