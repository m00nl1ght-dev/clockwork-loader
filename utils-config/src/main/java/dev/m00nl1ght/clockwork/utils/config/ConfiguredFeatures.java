package dev.m00nl1ght.clockwork.utils.config;

import dev.m00nl1ght.clockwork.utils.config.ConfigValue.TypeConfig;
import dev.m00nl1ght.clockwork.utils.config.ConfigValue.TypeConfigList;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec.Entry;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ConfiguredFeatures {

    public static final ConfigSpec CONFIG_SPEC = ConfigSpec.create("configured_feature");
    public static final Entry<String> CONFIG_ENTRY_TYPE = CONFIG_SPEC.put("type", ConfigValue.STRING).required();
    public static final Entry<String> CONFIG_ENTRY_NAME = CONFIG_SPEC.put("name", ConfigValue.STRING).required();
    public static final TypeConfig CONFIG_TYPE = CONFIG_SPEC.buildType();
    public static final TypeConfigList CONFIG_LIST_TYPE = ConfigValue.CLISTF(CONFIG_SPEC);

    private final Map<Class, Registry> registryMap = new HashMap<>();

    private boolean locked;

    public <T> void add(@NotNull Class<T> featureType,
                        @NotNull String featureName,
                        @NotNull T feature) {

        if (locked) throw new IllegalStateException("Registry is locked");
        registryFor(Objects.requireNonNull(featureType)).register(featureName, feature);
    }

    public <T, C> void add(@NotNull Class<T> featureType,
                           @NotNull ConfiguredFeatureProviders<C> providers,
                           @NotNull Config config,
                           @NotNull C context) {

        final var featureName = config.get(CONFIG_ENTRY_NAME);
        add(featureType, featureName, providers.newFeature(featureType, context, config));
    }

    public <T, C> void addAll(@NotNull Class<T> featureType,
                              @NotNull ConfiguredFeatureProviders<C> providers,
                              @NotNull Collection<@NotNull Config> configs,
                              @NotNull C context) {

        for (final var config : configs) add(featureType, providers, config, context);
    }

    public <T> T get(@NotNull Class<T> featureType, String featureName) {
        return registryFor(Objects.requireNonNull(featureType)).get(featureName);
    }

    public <T> Optional<T> getOptional(@NotNull Class<T> featureType, String featureName) {
        return registryFor(Objects.requireNonNull(featureType)).getOptional(featureName);
    }

    public <T> Map<String, T> getAllAsMap(@NotNull Class<T> featureType) {
        return registryFor(Objects.requireNonNull(featureType)).getRegisteredAsMap();
    }

    public <T> Set<T> getAll(@NotNull Class<T> featureType) {
        return registryFor(Objects.requireNonNull(featureType)).getRegistered();
    }

    public <T> Set<T> getAll(@NotNull Class<T> featureType, Collection<String> featureNames) {
        final var reg = registryFor(Objects.requireNonNull(featureType));
        return featureNames.stream().map(reg::get).collect(Collectors.toSet());
    }

    public void lock() {
        locked = true;
    }

    @SuppressWarnings("unchecked")
    private <T> @NotNull Registry<T> registryFor(@NotNull Class<T> forClass) {
        return (Registry<T>) registryMap.computeIfAbsent(forClass, Registry::new);
    }

}
