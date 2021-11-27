package dev.m00nl1ght.clockwork.utils.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigValue {

    private ConfigValue() {}

    // BASIC STATIC TYPES

    public static final TypeString                  T_STRING        = new TypeString(null);
    public static final TypeParsedList<String>      T_LIST_UF       = new TypeParsedList<>(T_STRING, true, false);
    public static final TypeParsedList<String>      T_LIST_F        = new TypeParsedList<>(T_STRING, true, true);
    public static final TypeParsedList<String>      T_LIST_U        = new TypeParsedList<>(T_STRING, false, false);
    public static final TypeParsedList<String>      T_LIST          = new TypeParsedList<>(T_STRING, false, true);
    public static final TypeBoolean                 T_BOOLEAN       = new TypeBoolean();
    public static final TypeInt                     T_INT           = new TypeInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static final TypeInt                     T_UINT          = new TypeInt(0, Integer.MAX_VALUE);
    public static final TypeFloat                   T_FLOAT         = new TypeFloat(Float.MIN_VALUE, Float.MAX_VALUE);
    public static final TypeFloat                   T_UFLOAT        = new TypeFloat(0f, Float.MAX_VALUE);
    public static final TypeConfig                  T_CONFIG        = new TypeConfig(null);
    public static final TypeConfigList              T_CLIST         = new TypeConfigList(null, false);
    public static final TypeConfigList              T_CLIST_F       = new TypeConfigList(null, true);

    // COMMON STATIC TYPES

    public static final TypeParsedCustom<Path>      T_PATH          = new TypeParsedCustom<>(Path.class, Path::of);
    public static final TypeParsedCustom<File>      T_FILE          = new TypeParsedCustom<>(File.class, File::new);
    public static final TypeParsedCustom<URI>       T_URI           = new TypeParsedCustom<>(URI.class, URI::create);

    // BASIC DYNAMIC TYPES

    public static <T> @NotNull TypeParsedList<T> T_LIST(@NotNull TypeParsed<T> elementType) {
        return new TypeParsedList<>(elementType, false, true);
    }

    public static <T> @NotNull TypeParsedList<T> T_LIST_U(@NotNull TypeParsed<T> elementType) {
        return new TypeParsedList<>(elementType, false, false);
    }

    public static <T> @NotNull TypeParsedList<T> T_LIST_F(@NotNull TypeParsed<T> elementType) {
        return new TypeParsedList<>(elementType, true, true);
    }

    public static <T> @NotNull TypeParsedList<T> T_LIST_UF(@NotNull TypeParsed<T> elementType) {
        return new TypeParsedList<>(elementType, true, false);
    }

    public static @NotNull TypeConfig T_CONFIG(@NotNull ConfigSpec spec) {
        return new TypeConfig(Objects.requireNonNull(spec));
    }

    public static @NotNull TypeConfigList T_CLIST(@NotNull ConfigSpec spec) {
        return new TypeConfigList(Objects.requireNonNull(spec), false);
    }

    public static @NotNull TypeConfigList T_CLIST_F(@NotNull ConfigSpec spec) {
        return new TypeConfigList(Objects.requireNonNull(spec), true);
    }

    public static <T> @NotNull TypeCustomList<T> T_CLIST(@NotNull TypeCustom<T> elementType) {
        return new TypeCustomList<>(elementType, false);
    }

    public static <T> @NotNull TypeCustomList<T> T_CLIST_F(@NotNull TypeCustom<T> elementType) {
        return new TypeCustomList<>(elementType, true);
    }

    public static <E extends Enum<E>> @NotNull TypeEnum<E> T_ENUM(@NotNull Class<E> enumClass) {
        return new TypeEnum<>(enumClass);
    }

    // TYPE CLASSES

    public abstract static class Type<T> {

        public abstract @Nullable T get(@NotNull Config config, @NotNull String key);

        public abstract void put(@NotNull ModifiableConfig config, @NotNull String key, @NotNull T value);

        public @Nullable ConfigException verify(@NotNull Config config, @NotNull String key, @NotNull T value) {
            return null;
        }

        public @Nullable ConfigException verify(@NotNull Config config, @NotNull String key) {
            final var value = get(config, key);
            return value == null ? null : verify(config, key, value);
        }

        public boolean isCompatible(@NotNull Type<?> other) {
            return this.getClass() == other.getClass();
        }

        public @NotNull BinaryOperator<T> getDefaultMergeFunction() {
            return ConfigValue::M_REPLACE;
        }

        public @Nullable T getDefaultValue() {
            return null;
        }

    }

    public abstract static class TypeParsed<T> extends Type<T> {

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

    public static class TypeParsedCustom<T> extends TypeParsed<T> {

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

    public static class TypeString extends TypeParsed<String> {

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
        public String getDefaultValue() {
            return "";
        }

    }

    public static class TypeBoolean extends TypeParsed<Boolean> {

        @Override
        public @NotNull Boolean parse(@NotNull Config config, @NotNull String key, @NotNull String value) {
            if (value.equalsIgnoreCase("true")) return true;
            if (value.equalsIgnoreCase("false")) return false;
            throw new ConfigException(config, key, value, "is not a boolean");
        }

        @Override
        public Boolean getDefaultValue() {
            return false;
        }

    }

    public static class TypeInt extends TypeParsed<Integer> {

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
        public Integer getDefaultValue() {
            return 0;
        }

    }

    public static class TypeFloat extends TypeParsed<Float> {

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
        public Float getDefaultValue() {
            return 0f;
        }

    }

    public static class TypeEnum<E extends Enum<E>> extends TypeParsed<E> {

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

    public static class TypeParsedList<T> extends Type<List<T>> {

        public final TypeParsed<T> elementType;
        public final boolean allowSingleton;
        public final boolean allowDuplicates;

        public TypeParsedList(@NotNull TypeParsed<T> elementType, boolean allowSingleton, boolean allowDuplicates) {
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
            if (!(other instanceof TypeParsedList)) return false;
            final var cOther = (TypeParsedList<?>) other;
            return elementType.isCompatible(cOther.elementType);
        }

        @Override
        public @NotNull BinaryOperator<List<T>> getDefaultMergeFunction() {
            return ConfigValue::M_JOIN;
        }

        @Override
        public List<T> getDefaultValue() {
            return List.of();
        }

    }

    public static class TypeConfig extends Type<Config> {

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
        public @NotNull BinaryOperator<Config> getDefaultMergeFunction() {
            return Config::merge;
        }

        @Override
        public Config getDefaultValue() {
            return Config.EMPTY;
        }

    }

    public static class TypeCustom<T> extends Type<T> {

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

    public static class TypeConfigList extends Type<List<Config>> {

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
        public @NotNull BinaryOperator<List<Config>> getDefaultMergeFunction() {
            return ConfigValue::M_JOIN;
        }

        @Override
        public List<Config> getDefaultValue() {
            return List.of();
        }

    }

    public static class TypeCustomList<T> extends Type<List<T>> {

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
            return elementType.isCompatible(cOther.elementType);
        }

        @Override
        public @NotNull BinaryOperator<List<T>> getDefaultMergeFunction() {
            return ConfigValue::M_JOIN;
        }

        @Override
        public List<T> getDefaultValue() {
            return List.of();
        }

    }

    // COMMON MERGE FUNCTIONS

    public static <T> T M_KEEP(T a, T b) { return a; }

    public static <T> T M_REPLACE(T a, T b) { return b; }

    public static boolean M_AND(boolean a, boolean b) { return a && b; }

    public static boolean M_OR(boolean a, boolean b) { return a || b; }

    public static <T> List<T> M_JOIN(List<T> a, List<T> b) {
        final var joined = new ArrayList<T>(a.size() + b.size());
        joined.addAll(a);
        joined.addAll(b);
        return joined;
    }

}
