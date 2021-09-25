package dev.m00nl1ght.clockwork.utils.config;

import dev.m00nl1ght.clockwork.utils.config.ConfigSpec.Entry;
import dev.m00nl1ght.clockwork.utils.config.impl.AttributesWrapper;
import dev.m00nl1ght.clockwork.utils.config.impl.EmptyConfig;
import dev.m00nl1ght.clockwork.utils.config.impl.ModifiableConfigImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface Config {

    Config EMPTY = EmptyConfig.INSTANCE;

    // VALUE TYPES

    TypeString          STRING      = new TypeString(null);
    TypeBoolean         BOOLEAN     = new TypeBoolean();
    TypeInt             INT         = new TypeInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
    TypeInt             UINT        = new TypeInt(0, Integer.MAX_VALUE);
    TypeFloat           FLOAT       = new TypeFloat(Float.MIN_VALUE, Float.MAX_VALUE);
    TypeFloat           UFLOAT      = new TypeFloat(0f, Float.MAX_VALUE);

    TypeList<String>    LIST        = new TypeList<>(STRING, false);
    TypeList<String>    LISTF       = new TypeList<>(STRING, true);

    TypeConfig          CONFIG      = new TypeConfig(null);

    TypeConfigList      CLIST       = new TypeConfigList(null, false);
    TypeConfigList      CLISTF      = new TypeConfigList(null, true);

    static <E extends Enum<E>> @NotNull Type<E> ENUM(@NotNull Class<E> enumClass) {
        return new TypeEnum<>(enumClass);
    }

    static <T> @NotNull Type<List<T>> LIST(@NotNull TypeParsed<T> elementType) {
        return new TypeList<>(elementType, false);
    }

    static <T> @NotNull Type<List<T>> LISTF(@NotNull TypeParsed<T> elementType) {
        return new TypeList<>(elementType, true);
    }

    static @NotNull TypeConfig CONFIG(@NotNull ConfigSpec spec) {
        return new TypeConfig(Objects.requireNonNull(spec));
    }

    static @NotNull TypeConfigList CLIST(@NotNull ConfigSpec spec) {
        return new TypeConfigList(Objects.requireNonNull(spec), false);
    }

    static @NotNull TypeConfigList CLISTF(@NotNull ConfigSpec spec) {
        return new TypeConfigList(Objects.requireNonNull(spec), true);
    }

    // FACTORY METHODS

    static @NotNull ModifiableConfig newConfig() {
        return new ModifiableConfigImpl(null);
    }

    static @NotNull ModifiableConfig newConfig(@Nullable ConfigSpec spec) {
        return new ModifiableConfigImpl(spec);
    }

    static @NotNull Config fromAttributes(@NotNull Attributes attributes) {
        return new AttributesWrapper(attributes, SimpleDataParser.DEFAULT_FORMAT, "");
    }

    static @NotNull Config fromAttributes(@NotNull Attributes attributes, @NotNull String keyPrefix) {
        return new AttributesWrapper(attributes, SimpleDataParser.DEFAULT_FORMAT, keyPrefix);
    }

    static @NotNull Config fromAttributes(@NotNull Attributes attributes,
                                          @NotNull SimpleDataParser.Format dataFormat,
                                          @NotNull String keyPrefix) {
        return new AttributesWrapper(attributes, dataFormat, keyPrefix);
    }

    // INSTANCE METHODS

    @NotNull Set<@NotNull String> getKeys();

    @Nullable String getString(@NotNull String key);

    @Nullable Config getSubconfig(@NotNull String key);

    @Nullable List<@NotNull String> getStrings(@NotNull String key);

    @Nullable List<? extends Config> getSubconfigs(@NotNull String key);

    default <T> @Nullable T get(@NotNull String key, @NotNull Type<T> valueType) {
        final var value = valueType.get(this, key);
        if (value == null) return null;
        final var exc = valueType.verify(this, key, value);
        if (exc != null) throw exc;
        return valueType.get(this, key);
    }

    default <T> @NotNull T getRequired(@NotNull String key, @NotNull Type<T> valueType) {
        final var value = get(key, valueType);
        if (value == null) throw new ConfigException(this, "Missing value for required entry " + key + " in " + this);
        return value;
    }

    default <T> @NotNull Optional<T> getOptional(@NotNull String key, @NotNull Type<T> valueType) {
        return Optional.ofNullable(get(key, valueType));
    }

    default <T> @NotNull T getOrDefault(@NotNull String key, @NotNull Type<T> valueType, @NotNull T defaultValue) {
        final var value = get(key, valueType);
        if (value == null) return Objects.requireNonNull(defaultValue);
        return value;
    }

    default <T> @Nullable T get(@NotNull Entry<T> entry) {
        final var spec = getSpec();
        final var value = entry.type.get(this, entry.key);
        if (spec == null || !spec.canApplyAs(entry.spec)) {
            final var exc = entry.type.verify(this, entry.key, value);
            if (exc != null) throw exc;
        }
        if (value != null) return value;
        if (entry.required) {
            throw new ConfigException(this, "Missing value for required entry " + entry.key + " in " + this);
        } else {
            return entry.defaultSupplier.apply(this);
        }
    }

    @NotNull Config copy(@Nullable ConfigSpec spec);

    default @NotNull Config copy() {
        return copy(getSpec());
    }

    @NotNull ModifiableConfig modifiableCopy(@Nullable ConfigSpec spec);

    default @NotNull ModifiableConfig modifiableCopy() {
        return modifiableCopy(getSpec());
    }

    default @NotNull Config asReadonly() {
        return this;
    }

    default @Nullable ConfigSpec getSpec() {
        return null;
    }

    // VALUE TYPE CLASSES

    abstract class Type<T> {

        public abstract @Nullable T get(@NotNull Config config, @NotNull String key);

        public abstract void put(@NotNull ModifiableConfig config, @NotNull String key, @NotNull T value);

        public @Nullable ConfigException verify(@NotNull Config config, @NotNull String key, @NotNull T value) {
            return null;
        }

        public @Nullable ConfigException verify(@NotNull Config config, @NotNull String key) {
            return verify(config, key, get(config, key));
        }

    }

    abstract class TypeParsed<T> extends Type<T> {

        @Override
        public @NotNull T get(@NotNull Config config, @NotNull String key) {
            final var raw = config.getString(key);
            return raw == null ? null : parse(config, key, raw);
        }

        @Override
        public void put(@NotNull ModifiableConfig config, @NotNull String key, @NotNull T value) {
            config.putString(key, asString(value));
        }

        public abstract @NotNull T parse(@NotNull Config config, @NotNull String key, @NotNull String value);

        public @NotNull String asString(@NotNull T value) {
            return value.toString();
        }

    }

    class TypeString extends TypeParsed<String> {

        public final Pattern pattern;

        public TypeString(@Nullable Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public @NotNull String parse(@NotNull Config config, @NotNull String key, @NotNull String value) {
            return value;
        }

        @Override
        public @Nullable ConfigException verify(@NotNull Config config, @NotNull String key, @NotNull String value) {
            if (pattern == null || pattern.matcher(value).matches()) return null;
            return new ConfigException(config, key, value, "does not match pattern " + pattern);
        }

    }

    class TypeBoolean extends TypeParsed<Boolean> {

        @Override
        public @NotNull Boolean parse(@NotNull Config config, @NotNull String key, @NotNull String value) {
            if (value.equalsIgnoreCase("true")) return true;
            if (value.equalsIgnoreCase("false")) return false;
            throw new ConfigException(config, key, value, "is not a boolean");
        }

    }

    class TypeInt extends TypeParsed<Integer> {

        public final int minValue;
        public final int maxValue;

        public TypeInt(int minValue, int maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        @Override
        public @NotNull Integer parse(@NotNull Config config, @NotNull String key, @NotNull String value) {
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

    }

    class TypeFloat extends TypeParsed<Float> {

        public final float minValue;
        public final float maxValue;

        public TypeFloat(float minValue, float maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        @Override
        public @NotNull Float parse(@NotNull Config config, @NotNull String key, @NotNull String value) {
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

    }

    class TypeEnum<E extends Enum<E>> extends TypeParsed<E> {

        public final Class<E> enumClass;

        public TypeEnum(Class<E> enumClass) {
            this.enumClass = Objects.requireNonNull(enumClass);
        }

        @Override
        public @NotNull String asString(@NotNull E value) {
            return value.name();
        }

        @Override
        public @NotNull E parse(@NotNull Config config, @NotNull String key, @NotNull String value) {
            try {
                return Enum.valueOf(enumClass, value);
            } catch (IllegalArgumentException e) {
                throw new ConfigException(config, key, value, "is not a value of enum " + enumClass.getSimpleName());
            }
        }

    }

    class TypeList<T> extends Type<List<T>> {

        public final TypeParsed<T> elementType;
        public final boolean allowSingleton;

        public TypeList(@NotNull TypeParsed<T> elementType, boolean allowSingleton) {
            this.elementType = Objects.requireNonNull(elementType);
            this.allowSingleton = allowSingleton;
        }

        @Override
        public @Nullable List<T> get(@NotNull Config config, @NotNull String key) {
            final var raw = config.getStrings(key);
            if (raw != null) return raw.stream()
                    .map(v -> elementType.parse(config, key, v))
                    .collect(Collectors.toList());
            if (!allowSingleton) return null;
            final var singleton = config.getString(key);
            return singleton == null ? null : List.of(elementType.parse(config, key, singleton));
        }

        @Override
        public void put(@NotNull ModifiableConfig config, @NotNull String key, @NotNull List<T> value) {
            config.putStrings(key, value.stream().map(elementType::asString).collect(Collectors.toList()));
        }

        @Override
        public @Nullable ConfigException verify(@NotNull Config config, @NotNull String key, @NotNull List<T> value) {
            return value.stream()
                    .map(element -> elementType.verify(config, key, element))
                    .filter(Objects::nonNull)
                    .findFirst().orElse(null);
        }

    }

    class TypeConfig extends Type<Config> {

        public final ConfigSpec spec;

        public TypeConfig(@Nullable ConfigSpec spec) {
            this.spec = spec;
        }

        @Override
        public @Nullable Config get(@NotNull Config config, @NotNull String key) {
            return config.getSubconfig(key);
        }

        @Override
        public void put(@NotNull ModifiableConfig config, @NotNull String key, @NotNull Config value) {
            config.putSubconfig(key, value);
        }

        @Override
        public @Nullable ConfigException verify(@NotNull Config config, @NotNull String key, @NotNull Config value) {
            if (spec == null) return null;
            final var vSpec = value.getSpec();
            if (vSpec == null)
                return new ConfigException(config, key, value, "does not have required spec " + spec);
            if (!vSpec.canApplyAs(spec))
                return new ConfigException(config, key, value, "has incompatible spec " + vSpec);
            return null;
        }

    }

    class TypeConfigList extends Type<List<? extends Config>> {

        public final ConfigSpec spec;
        public final boolean allowSingleton;

        public TypeConfigList(@Nullable ConfigSpec spec, boolean allowSingleton) {
            this.spec = spec;
            this.allowSingleton = allowSingleton;
        }

        @Override
        public @Nullable List<? extends Config> get(@NotNull Config config, @NotNull String key) {
            final var list = config.getSubconfigs(key);
            if (list != null) return list;
            if (!allowSingleton) return null;
            final var singleton = config.getSubconfig(key);
            return singleton == null ? null : List.of(singleton);
        }

        @Override
        public void put(@NotNull ModifiableConfig config, @NotNull String key, @NotNull List<? extends Config> value) {
            config.putSubconfigs(key, value);
        }

        @Override
        public @Nullable ConfigException verify(@NotNull Config config, @NotNull String key, @NotNull List<? extends Config> value) {
            if (spec == null) return null;
            for (final var element : value) {
                final var vSpec = element.getSpec();
                if (vSpec == null)
                    return new ConfigException(config, key, value, "does not have required spec " + spec);
                if (!vSpec.canApplyAs(spec))
                    return new ConfigException(config, key, element, "has incompatible spec " + vSpec);
            }
            return null;
        }

    }

}
