package dev.m00nl1ght.clockwork.utils.config;

import dev.m00nl1ght.clockwork.utils.config.ConfigSpec.Entry;
import dev.m00nl1ght.clockwork.utils.config.ConfigValue.Type;
import dev.m00nl1ght.clockwork.utils.config.impl.CachedConfig;
import dev.m00nl1ght.clockwork.utils.config.impl.EmptyConfig;
import dev.m00nl1ght.clockwork.utils.config.impl.MinimalSDPConfig;
import dev.m00nl1ght.clockwork.utils.config.impl.ModifiableConfigImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.stream.Collectors;

public interface Config {

    Config EMPTY = EmptyConfig.INSTANCE;

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

    default @Nullable Object getObject(@NotNull String key) {
        final var spec = getSpec();
        final var entry = spec == null ? null : spec.getEntry(key);
        if (entry != null) return get(key, entry.type);
        final var asString = getString(key);
        if (asString != null) return asString;
        final var asConfig = getSubconfig(key);
        if (asConfig != null) return asConfig;
        final var asList = getStrings(key);
        if (asList != null) return asList;
        return getSubconfigs(key);
    }

    default @NotNull Map<@NotNull String, @NotNull Object> toMap() {
        return getKeys().stream().collect(Collectors.toMap(Function.identity(), this::getObject));
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

    // STATIC FACTORIES

    static @NotNull ModifiableConfig newConfig() {
        return new ModifiableConfigImpl(null);
    }

    static @NotNull ModifiableConfig newConfig(@Nullable ConfigSpec spec) {
        return new ModifiableConfigImpl(spec);
    }

    static @NotNull Config fromMapLike(@NotNull Function<? super String, ?> valueProvider,
                                       @NotNull Supplier<? extends Collection<?>> keyProvider,
                                       @NotNull SimpleDataParser.Format dataFormat,
                                       @NotNull String keyPrefix,
                                       @NotNull String name) {
        return new MinimalSDPConfig(valueProvider, keyProvider, dataFormat, keyPrefix, name);
    }

    static @NotNull Config fromMapLike(@NotNull Function<? super String, ?> valueProvider,
                                       @NotNull Supplier<? extends Collection<?>> keyProvider,
                                       @NotNull String keyPrefix,
                                       @NotNull String name) {
        return new MinimalSDPConfig(valueProvider, keyProvider, SimpleDataParser.DEFAULT_FORMAT, keyPrefix, name);
    }

    static @NotNull Config fromMapLike(@NotNull Function<? super String, ?> valueProvider,
                                       @NotNull Supplier<? extends Collection<?>> keyProvider,
                                       @NotNull String name) {
        return new MinimalSDPConfig(valueProvider, keyProvider, SimpleDataParser.DEFAULT_FORMAT, "", name);
    }

    static @NotNull Config fromMapLike(@NotNull Function<? super String, ?> valueProvider,
                                       @NotNull SimpleDataParser.Format dataFormat,
                                       @NotNull String keyPrefix,
                                       @NotNull String name) {
        return new MinimalSDPConfig(valueProvider, null, dataFormat, keyPrefix, name);
    }

    static @NotNull Config fromMapLike(@NotNull Function<? super String, ?> valueProvider,
                                       @NotNull String keyPrefix,
                                       @NotNull String name) {
        return new MinimalSDPConfig(valueProvider, null, SimpleDataParser.DEFAULT_FORMAT, keyPrefix, name);
    }

    static @NotNull Config fromMapLike(@NotNull Function<? super String, ?> valueProvider,
                                       @NotNull String name) {
        return new MinimalSDPConfig(valueProvider, null, SimpleDataParser.DEFAULT_FORMAT, "", name);
    }

    static @NotNull Config fromMap(@NotNull Map<String, ?> map,
                                   @NotNull SimpleDataParser.Format dataFormat,
                                   @NotNull String keyPrefix,
                                   @NotNull String name) {
        return new MinimalSDPConfig(map::get, map::keySet, dataFormat, keyPrefix, name);
    }

    static @NotNull Config fromMap(@NotNull Map<String, ?> map, @NotNull String keyPrefix, @NotNull String name) {
        return fromMap(map, SimpleDataParser.DEFAULT_FORMAT, keyPrefix, name);
    }

    static @NotNull Config fromMap(@NotNull Map<String, ?> map, @NotNull String name) {
        return fromMap(map, SimpleDataParser.DEFAULT_FORMAT, "", name);
    }

    static @NotNull Config fromAttributes(@NotNull Attributes attributes, @NotNull String keyPrefix) {
        return fromMapLike(attributes::getValue, attributes::keySet, keyPrefix, "Attributes");
    }

    static @NotNull Config fromAttributes(@NotNull Attributes attributes) {
        return fromAttributes(attributes, "");
    }

    static @NotNull Config fromProperties(@NotNull Properties properties, @NotNull String keyPrefix) {
        return fromMapLike(properties::getProperty, properties::keySet, keyPrefix, "Properties");
    }

    static @NotNull Config fromProperties(@NotNull Properties properties) {
        return fromProperties(properties, "");
    }

    static @NotNull Config fromSystemProperties(@NotNull String keyPrefix) {
        return fromProperties(System.getProperties(), keyPrefix);
    }

    static @NotNull Config fromSystemProperties() {
        return fromProperties(System.getProperties(), "");
    }

    static @NotNull Config merge(Config base, Config other) {
        final var spec = base.getSpec();
        final var merged = base.modifiableCopy();
        final var allowAdditional = spec == null || spec.areAdditionalEntriesAllowed();

        for (final var key : other.getKeys()) {
            final var entry = spec == null ? null : spec.getEntry(key);
            if (entry != null) {
                merge(merged, other, entry);
            } else if (allowAdditional) {
                merge(merged, other, key);
            }
        }

        return merged;
    }

    private static <T> void merge(ModifiableConfig config, Config other, Entry<T> entry) {
        final var otherValue = other.get(entry.key, entry.type);
        if (otherValue == null) return;
        config.put(entry, otherValue);
    }

    private static void merge(ModifiableConfig config, Config other, String key) {

        final var asString = other.getString(key);
        if (asString != null) {
            config.putString(key, asString);
            return;
        }

        final var asConfig = other.getSubconfig(key);
        if (asConfig != null) {
            final var base = config.getSubconfig(key);
            config.putSubconfig(key, base == null ? asConfig : merge(base, asConfig));
            return;
        }

        final var asList = other.getStrings(key);
        if (asList != null) {
            final var base = config.getStrings(key);
            config.putStrings(key, base == null ? asList : ConfigValue.M_JOIN(base, asList));
            return;
        }

        final var asConfigList = other.getSubconfigs(key);
        if (asConfigList != null) {
            final var base = config.getSubconfigs(key);
            config.putSubconfigs(key, base == null ? asConfigList : ConfigValue.M_JOIN(base, asConfigList));
        }
    }

    static @NotNull Config merge(List<Config> configs) {
        return configs.stream().reduce(Config::merge).orElseThrow();
    }

    static @NotNull Config withCache(Config config) {
        return new CachedConfig(Objects.requireNonNull(config));
    }

}
