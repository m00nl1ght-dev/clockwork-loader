package dev.m00nl1ght.clockwork.utils.config;

import dev.m00nl1ght.clockwork.utils.config.ConfigSpec.Entry;
import dev.m00nl1ght.clockwork.utils.config.impl.AttributesWrapper;
import dev.m00nl1ght.clockwork.utils.config.impl.EmptyConfig;
import dev.m00nl1ght.clockwork.utils.config.impl.ModifiableConfigImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
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

    TypeList<String>    LIST        = new TypeList<>(STRING, false, true);
    TypeList<String>    LIST_U      = new TypeList<>(STRING, false, false);
    TypeList<String>    LIST_F      = new TypeList<>(STRING, true, true);
    TypeList<String>    LIST_UF     = new TypeList<>(STRING, true, false);

    TypeConfig          CONFIG      = new TypeConfig(null);

    TypeConfigList      CLIST       = new TypeConfigList(null, false);
    TypeConfigList      CLISTF      = new TypeConfigList(null, true);

    static <E extends Enum<E>> @NotNull TypeEnum<E> ENUM(@NotNull Class<E> enumClass) {
        return new TypeEnum<>(enumClass);
    }

    static <T> @NotNull TypeParsedCustom<T> CUSTOM(@NotNull Class<T> targetClass,
                                                   @NotNull Function<@NotNull String, @NotNull T> factory) {

        return new TypeParsedCustom<>(targetClass, factory);
    }

    static <T> @NotNull TypeCustom<T> CUSTOM(@NotNull Class<T> targetClass,
                                             @NotNull Function<@NotNull Config, @NotNull T> fromConfig,
                                             @NotNull Function<@NotNull T, @NotNull Config> toConfig) {

        return new TypeCustom<>(targetClass, fromConfig, toConfig);
    }

    static <T> @NotNull TypeList<T> LIST(@NotNull TypeParsed<T> elementType) {
        return new TypeList<>(elementType, false, true);
    }

    static <T> @NotNull TypeList<T> LIST_U(@NotNull TypeParsed<T> elementType) {
        return new TypeList<>(elementType, false, false);
    }

    static <T> @NotNull TypeList<T> LIST_F(@NotNull TypeParsed<T> elementType) {
        return new TypeList<>(elementType, true, true);
    }

    static <T> @NotNull TypeList<T> LIST_UF(@NotNull TypeParsed<T> elementType) {
        return new TypeList<>(elementType, true, false);
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

    static <T> @NotNull TypeCustomList<T> CLIST(@NotNull TypeCustom<T> elementType) {
        return new TypeCustomList<>(elementType, false);
    }

    static <T> @NotNull TypeCustomList<T> CLISTF(@NotNull TypeCustom<T> elementType) {
        return new TypeCustomList<>(elementType, true);
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

    @Nullable List<Config> getSubconfigs(@NotNull String key);

    default <T> @Nullable T get(@NotNull String key, @NotNull Type<T> valueType) {
        final var value = valueType.get(this, key);
        if (value == null) return null;
        final var exc = valueType.verify(this, key, value);
        if (exc != null) throw exc;
        return value;
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
        if (value != null) {
            if (spec == null || !spec.canApplyAs(entry.spec)) {
                final var exc = entry.type.verify(this, entry.key, value);
                if (exc != null) throw exc;
            }
            return value;
        } else if (entry.required) {
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

    default void requireSpec(@Nullable ConfigSpec reqSpec) {
        if (reqSpec == null) return;
        final var vSpec = getSpec();
        if (vSpec == null)
            throw new ConfigException(this, this + " does not have required spec " + reqSpec);
        if (!vSpec.canApplyAs(reqSpec))
            throw new ConfigException(this, this + " has incompatible spec " + vSpec + " where " + reqSpec + " is required");
    }

    // VALUE TYPE CLASSES

    abstract class Type<T> {

        public abstract @Nullable T get(@NotNull Config config, @NotNull String key);

        public abstract void put(@NotNull ModifiableConfig config, @NotNull String key, @NotNull T value);

        public @Nullable ConfigException verify(@NotNull Config config, @NotNull String key, @NotNull T value) {
            return null;
        }

        public @Nullable ConfigException verify(@NotNull Config config, @NotNull String key) {
            final var value = get(config, key);
            return value == null ? null : verify(config, key, value);
        }

        public boolean isCompatible(Type<?> other) {
            return this.getClass() == other.getClass();
        }

        public T getDefault() {
            return null;
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

    class TypeParsedCustom<T> extends TypeParsed<T> {

        private final Class<T> parsedClass;
        private final Function<String, T> parser;

        public TypeParsedCustom(@NotNull Class<T> parsedClass, @NotNull Function<@NotNull String, @NotNull T> parser) {
            this.parsedClass = Objects.requireNonNull(parsedClass);
            this.parser = Objects.requireNonNull(parser);
        }

        @Override
        public @NotNull T parse(@NotNull Config config, @NotNull String key, @NotNull String value) {
            try {
                return parser.apply(value);
            } catch (Exception e) {
                throw new ConfigException(config, key, value, "is not a valid " + parsedClass.getSimpleName(), e);
            }
        }

        @Override
        public boolean isCompatible(Type<?> other) {
            if (!(other instanceof TypeParsedCustom)) return false;
            final var cOther = (TypeParsedCustom<?>) other;
            return cOther.parsedClass == parsedClass;
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

        @Override
        public boolean isCompatible(Type<?> other) {
            if (!(other instanceof TypeString)) return false;
            final var cOther = (TypeString) other;
            return cOther.pattern.pattern().equals(pattern.pattern());
        }

        @Override
        public String getDefault() {
            return "";
        }

    }

    class TypeBoolean extends TypeParsed<Boolean> {

        @Override
        public @NotNull Boolean parse(@NotNull Config config, @NotNull String key, @NotNull String value) {
            if (value.equalsIgnoreCase("true")) return true;
            if (value.equalsIgnoreCase("false")) return false;
            throw new ConfigException(config, key, value, "is not a boolean");
        }

        @Override
        public Boolean getDefault() {
            return false;
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

        @Override
        public boolean isCompatible(Type<?> other) {
            if (!(other instanceof TypeInt)) return false;
            final var cOther = (TypeInt) other;
            return cOther.minValue >= minValue && cOther.maxValue <= maxValue;
        }

        @Override
        public Integer getDefault() {
            return 0;
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

        @Override
        public boolean isCompatible(Type<?> other) {
            if (!(other instanceof TypeFloat)) return false;
            final var cOther = (TypeFloat) other;
            return cOther.minValue >= minValue && cOther.maxValue <= maxValue;
        }

        @Override
        public Float getDefault() {
            return 0f;
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

        @Override
        public boolean isCompatible(Type<?> other) {
            if (!(other instanceof TypeEnum)) return false;
            final var cOther = (TypeEnum<?>) other;
            return cOther.enumClass == enumClass;
        }

    }

    class TypeList<T> extends Type<List<T>> {

        public final TypeParsed<T> elementType;
        public final boolean allowSingleton;
        public final boolean allowDuplicates;

        public TypeList(@NotNull TypeParsed<T> elementType, boolean allowSingleton, boolean allowDuplicates) {
            this.elementType = Objects.requireNonNull(elementType);
            this.allowSingleton = allowSingleton;
            this.allowDuplicates = allowDuplicates;
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
            if (!allowDuplicates) {
                final var tempSet = new HashSet<>();
                for (T element : value) {
                    if (!tempSet.add(element))
                        return new ConfigException(config, key, value, "is present more than once, but duplicates are not allowed");
                }
            }
            return value.stream()
                    .map(element -> elementType.verify(config, key, element))
                    .filter(Objects::nonNull)
                    .findFirst().orElse(null);
        }

        @Override
        public boolean isCompatible(Type<?> other) {
            if (!(other instanceof TypeList)) return false;
            final var cOther = (TypeList<?>) other;
            return cOther.elementType.isCompatible(cOther.elementType);
        }

        @Override
        public List<T> getDefault() {
            return List.of();
        }

    }

    class TypeConfig extends Type<Config> {

        public final ConfigSpec spec;

        public TypeConfig(@Nullable ConfigSpec spec) {
            this.spec = spec;
            if (spec != null) spec.lock();
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
                return new ConfigException(config, key, value, "has incompatible spec " + vSpec + " where " + spec + " is required");
            return null;
        }

        @Override
        public boolean isCompatible(Type<?> other) {
            if (!(other instanceof TypeConfig)) return false;
            final var cOther = (TypeConfig) other;
            return ConfigSpec.canApply(cOther.spec, spec);
        }

        @Override
        public Config getDefault() {
            return EMPTY;
        }

    }

    class TypeCustom<T> extends Type<T> {

        private final Class<T> targetClass;
        private final Function<Config, T> fromConfig;
        private final Function<T, Config> toConfig;

        public TypeCustom(@NotNull Class<T> targetClass,
                          @NotNull Function<@NotNull Config, @NotNull T> fromConfig,
                          @NotNull Function<@NotNull T, @NotNull Config> toConfig) {

            this.targetClass = Objects.requireNonNull(targetClass);
            this.fromConfig = Objects.requireNonNull(fromConfig);
            this.toConfig = Objects.requireNonNull(toConfig);
        }

        @Override
        public @Nullable T get(@NotNull Config config, @NotNull String key) {
            return fromConfig(config, key, config.getSubconfig(key));
        }

        public @Nullable T fromConfig(@NotNull Config config, @NotNull String key, @Nullable Config value) {
            if (value == null) return null;
            try {
                return fromConfig.apply(value);
            } catch (Exception e) {
                throw new ConfigException(config, key, value, "is not a valid " + targetClass.getSimpleName(), e);
            }
        }

        @Override
        public void put(@NotNull ModifiableConfig config, @NotNull String key, @NotNull T value) {
            config.putSubconfig(key, toConfig(value));
        }

        public @NotNull Config toConfig(@NotNull T value) {
            return toConfig.apply(Objects.requireNonNull(value));
        }

        @Override
        public boolean isCompatible(Type<?> other) {
            if (!(other instanceof TypeCustom)) return false;
            final var cOther = (TypeCustom<?>) other;
            return cOther.targetClass == targetClass;
        }

    }

    class TypeConfigList extends Type<List<Config>> {

        public final ConfigSpec spec;
        public final boolean allowSingleton;

        public TypeConfigList(@Nullable ConfigSpec spec, boolean allowSingleton) {
            this.spec = spec;
            this.allowSingleton = allowSingleton;
        }

        @Override
        public @Nullable List<Config> get(@NotNull Config config, @NotNull String key) {
            final var list = config.getSubconfigs(key);
            if (list != null) return list;
            if (!allowSingleton) return null;
            final var singleton = config.getSubconfig(key);
            return singleton == null ? null : List.of(singleton);
        }

        @Override
        public void put(@NotNull ModifiableConfig config, @NotNull String key, @NotNull List<Config> value) {
            config.putSubconfigs(key, value);
        }

        @Override
        public @Nullable ConfigException verify(@NotNull Config config, @NotNull String key, @NotNull List<Config> value) {
            if (spec == null) return null;
            for (final var element : value) {
                final var vSpec = element.getSpec();
                if (vSpec == null)
                    return new ConfigException(config, key, value, "does not have required spec " + spec);
                if (!vSpec.canApplyAs(spec))
                    return new ConfigException(config, key, element, "has incompatible spec " + vSpec + " where " + spec + " is required");
            }
            return null;
        }

        @Override
        public boolean isCompatible(Type<?> other) {
            if (!(other instanceof TypeConfigList)) return false;
            final var cOther = (TypeConfigList) other;
            return ConfigSpec.canApply(cOther.spec, spec);
        }

        @Override
        public List<Config> getDefault() {
            return List.of();
        }

    }

    class TypeCustomList<T> extends Type<List<T>> {

        public final TypeCustom<T> elementType;
        public final boolean allowSingleton;

        public TypeCustomList(@NotNull TypeCustom<T> elementType, boolean allowSingleton) {
            this.elementType = Objects.requireNonNull(elementType);
            this.allowSingleton = allowSingleton;
        }

        @Override
        public @Nullable List<T> get(@NotNull Config config, @NotNull String key) {
            final var raw = config.getSubconfigs(key);
            if (raw != null) return raw.stream()
                    .map(v -> elementType.fromConfig(config, key, v))
                    .collect(Collectors.toList());
            if (!allowSingleton) return null;
            final var singleton = config.getSubconfig(key);
            return singleton == null ? null : List.of(elementType.fromConfig(config, key, singleton));
        }

        @Override
        public void put(@NotNull ModifiableConfig config, @NotNull String key, @NotNull List<T> value) {
            config.putSubconfigs(key, value.stream().map(elementType::toConfig).collect(Collectors.toList()));
        }

        @Override
        public @Nullable ConfigException verify(@NotNull Config config, @NotNull String key, @NotNull List<T> value) {
            return value.stream()
                    .map(element -> elementType.verify(config, key, element))
                    .filter(Objects::nonNull)
                    .findFirst().orElse(null);
        }

        @Override
        public boolean isCompatible(Type<?> other) {
            if (!(other instanceof TypeCustomList)) return false;
            final var cOther = (TypeCustomList<?>) other;
            return cOther.elementType.isCompatible(cOther.elementType);
        }

        @Override
        public List<T> getDefault() {
            return List.of();
        }

    }

}
