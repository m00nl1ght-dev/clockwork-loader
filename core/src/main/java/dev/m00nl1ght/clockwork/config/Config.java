package dev.m00nl1ght.clockwork.config;

import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Config {

    Config EMPTY = new Empty();

    Set<String> getKeys();

    String getOrNull(String key);

    Config getSubconfigOrNull(String key);

    List<String> getListOrNull(String key);

    List<Config> getSubconfigListOrNull(String key);

    default String get(String key) {
        final var value = getOrNull(key);
        if (value == null) throw FormatUtil.rtExc("Missing value [] in config []", key, this);
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
        if (value == null) throw FormatUtil.rtExc("Missing subconfig [] in config []", key, this);
        return value;
    }

    default Optional<Config> getOptionalSubconfig(String key) {
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
        if (value == null) throw FormatUtil.rtExc("Missing list [] in config []", key, this);
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

    default List<Config> getSubconfigList(String key) {
        final var value = getSubconfigListOrNull(key);
        if (value == null) throw FormatUtil.rtExc("Missing list [] in config []", key, this);
        return value;
    }

    default Optional<List<Config>> getOptionalSubconfigList(String key) {
        return Optional.ofNullable(getSubconfigListOrNull(key));
    }

    default List<Config> getSubconfigListOrEmpty(String key) {
        final var value = getSubconfigListOrNull(key);
        return value == null ? List.of() : value;
    }

    default List<Config> getSubconfigListOrSingletonOrEmpty(String key) {
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
            throw FormatUtil.rtExc("Inavalid value [] in config [] ([] is not an Integer)", key, this, value);
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
            throw FormatUtil.rtExc("Inavalid value [] in config [] ([] is not a Float)", key, this, value);
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
            throw FormatUtil.rtExc("Inavalid value [] in config [] ([] is not a Boolean)", key, this, value);
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
        return found.orElseThrow(() -> FormatUtil.rtExc("Inavalid value [] in config [] (enum constant [] does not exist)", key, this, value));
    }

    Config immutable();

    default StrictConfig strict() {
        return new StrictConfig(this);
    }

    class Empty implements Config {

        private Empty() {}

        @Override
        public Set<String> getKeys() {
            return Set.of();
        }

        @Override
        public String getOrNull(String key) {
            return null;
        }

        @Override
        public Config getSubconfigOrNull(String key) {
            return null;
        }

        @Override
        public List<String> getListOrNull(String key) {
            return null;
        }

        @Override
        public List<Config> getSubconfigListOrNull(String key) {
            return null;
        }

        @Override
        public Config immutable() {
            return this;
        }

    }

}
