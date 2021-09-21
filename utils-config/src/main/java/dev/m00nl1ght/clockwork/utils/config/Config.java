package dev.m00nl1ght.clockwork.utils.config;

import dev.m00nl1ght.clockwork.utils.config.impl.AttributesWrapper;
import dev.m00nl1ght.clockwork.utils.config.impl.EmptyConfig;
import dev.m00nl1ght.clockwork.utils.config.impl.ModifiableConfigImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.jar.Attributes;
import java.util.regex.Pattern;

public interface Config {

    Config EMPTY = EmptyConfig.INSTANCE;

    // VALUE TYPES

    Type<String>    STRING      = new TypeString(null);
    Type<Boolean>   BOOLEAN     = new TypeBoolean();
    Type<Integer>   INT         = new TypeInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
    Type<Integer>   UINT        = new TypeInt(0, Integer.MAX_VALUE);
    Type<Float>     FLOAT       = new TypeFloat(Float.MIN_VALUE, Float.MAX_VALUE);
    Type<Float>     UFLOAT      = new TypeFloat(0f, Float.MAX_VALUE);

    static <E extends Enum<E>> Type<E> ENUM(Class<E> enumClass) {
        return new TypeEnum<>(enumClass);
    }

    // FACTORY METHODS

    static ModifiableConfig newConfig() {
        return new ModifiableConfigImpl();
    }

    static Config fromAttributes(Attributes attributes) {
        return new AttributesWrapper(attributes, SimpleDataParser.DEFAULT_FORMAT, "");
    }

    static Config fromAttributes(Attributes attributes, String keyPrefix) {
        return new AttributesWrapper(attributes, SimpleDataParser.DEFAULT_FORMAT, keyPrefix);
    }

    static Config fromAttributes(Attributes attributes, SimpleDataParser.Format dataFormat, String keyPrefix) {
        return new AttributesWrapper(attributes, dataFormat, keyPrefix);
    }

    // INSTANCE METHODS

    Set<String> getKeys();

    String getOrNull(String key);

    Config getSubconfigOrNull(String key);

    List<String> getListOrNull(String key);

    List<? extends Config> getSubconfigListOrNull(String key);

    default String get(String key) {
        final var value = getOrNull(key);
        if (value == null) throw new ConfigException(this, "Missing value for entry " + key + " in " + this);
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
        if (value == null) throw new ConfigException(this, "Missing subconfig for entry " + key + " in " + this);
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
        if (value == null) throw new ConfigException(this, "Missing list for entry " + key + " in " + this);
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
        if (value == null) throw new ConfigException(this, "Missing list for entry " + key + " in " + this);
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

    default <T> T getOrNull(String key, Type<T> valueType) {
        final var raw = getOrNull(key);
        if (raw == null) return null;
        final var value = valueType.get(this, key, raw);
        final var exc = valueType.verify(this, key, value);
        if (exc != null) throw exc;
        return value;
    }

    default <T> T get(String key, Type<T> valueType) {
        final var value = getOrNull(key, valueType);
        if (value == null) throw new ConfigException(this, "Missing value for entry " + key + " in " + this);
        return value;
    }

    default <T> Optional<T> getOptional(String key, Type<T> valueType) {
        return Optional.ofNullable(getOrNull(key, valueType));
    }

    default <T> T getOrDefault(String key, Type<T> valueType, T defaultValue) {
        final var value = getOrNull(key, valueType);
        if (value == null) return defaultValue;
        return value;
    }

    Config copy();

    ModifiableConfig modifiableCopy();

    default Config asReadonly() {
        return this;
    }

    // VALUE TYPE CLASSES

    abstract class Type<T> {

        public abstract @NotNull T get(@NotNull Config config, @NotNull String key, @NotNull String value);

        public @Nullable ConfigException verify(@NotNull Config config, @NotNull String key, @NotNull T value) {
            return null;
        }

        public abstract @NotNull Class<T> getValueClass();

    }

    class TypeString extends Type<String> {

        public final Pattern pattern;

        public TypeString(@Nullable Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public @NotNull String get(@NotNull Config config, @NotNull String key, @NotNull String value) {
            return value;
        }

        @Override
        public @Nullable ConfigException verify(@NotNull Config config, @NotNull String key, @NotNull String value) {
            if (pattern == null || pattern.matcher(value).matches()) return null;
            return new ConfigException(config, key, value, "does not match pattern " + pattern);
        }

        @Override
        public @NotNull Class<String> getValueClass() {
            return String.class;
        }

    }

    class TypeBoolean extends Type<Boolean> {

        @Override
        public @NotNull Boolean get(@NotNull Config config, @NotNull String key, @NotNull String value) {
            if (value.equalsIgnoreCase("true")) return true;
            if (value.equalsIgnoreCase("false")) return false;
            throw new ConfigException(config, key, value, "is not a boolean");
        }

        @Override
        public @NotNull Class<Boolean> getValueClass() {
            return Boolean.class;
        }

    }


    class TypeInt extends Type<Integer> {

        public final int minValue;
        public final int maxValue;

        public TypeInt(int minValue, int maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        @Override
        public @NotNull Integer get(@NotNull Config config, @NotNull String key, @NotNull String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new ConfigException(config, key, value, "is not an integer");
            }
        }

        @Override
        public @Nullable ConfigException verify(@NotNull Config config, @NotNull String key, @NotNull Integer value) {
            if (value >= minValue && value <= maxValue) return null;
            return new ConfigException(config, key, value, "is not in range [" + minValue + ";" + maxValue + "]");
        }

        @Override
        public @NotNull Class<Integer> getValueClass() {
            return Integer.class;
        }

    }

    class TypeFloat extends Type<Float> {

        public final float minValue;
        public final float maxValue;

        public TypeFloat(float minValue, float maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        @Override
        public @NotNull Float get(@NotNull Config config, @NotNull String key, @NotNull String value) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                throw new ConfigException(config, key, value, "is not a float");
            }
        }

        @Override
        public @Nullable ConfigException verify(@NotNull Config config, @NotNull String key, @NotNull Float value) {
            if (value >= minValue && value <= maxValue) return null;
            return new ConfigException(config, key, value, "is not in range [" + minValue + ";" + maxValue + "]");
        }

        @Override
        public @NotNull Class<Float> getValueClass() {
            return Float.class;
        }

    }

    class TypeEnum<E extends Enum<E>> extends Type<E> {

        public final Class<E> enumClass;

        public TypeEnum(Class<E> enumClass) {
            this.enumClass = Objects.requireNonNull(enumClass);
        }

        @Override
        public @NotNull E get(@NotNull Config config, @NotNull String key, @NotNull String value) {
            try {
                return Enum.valueOf(enumClass, value);
            } catch (IllegalArgumentException e) {
                throw new ConfigException(config, key, value, "is not a value of enum " + enumClass.getSimpleName());
            }
        }

        @Override
        public @NotNull Class<E> getValueClass() {
            return enumClass;
        }

    }

}
