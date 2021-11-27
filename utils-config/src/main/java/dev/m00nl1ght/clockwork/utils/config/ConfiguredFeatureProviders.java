package dev.m00nl1ght.clockwork.utils.config;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

public class ConfiguredFeatureProviders<C> {

    private final Map<Class, Registry> registryMap = new HashMap<>();

    private boolean locked;

    public <T> @NotNull T newFeature(@NotNull Class<T> featureType,
                                     @NotNull C context,
                                     @NotNull Config config) {

        final var providerName = config.get(ConfiguredFeatures.SPEC.FEATURE_TYPE);
        final var provider = registryFor(Objects.requireNonNull(featureType)).get(providerName);
        return provider.apply(Objects.requireNonNull(context), config);
    }

    public <T> void register(@NotNull Class<T> featureType,
                             @NotNull String providerName,
                             @NotNull BiFunction<? super C, Config, ? extends T> provider) {

        if (locked) throw new IllegalStateException("Registry is locked");
        registryFor(Objects.requireNonNull(featureType)).register(providerName, provider);
    }

    public @NotNull Set<@NotNull String> getRegistered(@NotNull Class<?> featureType) {
        return registryFor(Objects.requireNonNull(featureType)).getRegisteredAsMap().keySet();
    }

    public void lock() {
        locked = true;
    }

    @SuppressWarnings("unchecked")
    private <T> @NotNull Registry<BiFunction<? super C, Config, ? extends T>> registryFor(@NotNull Class<T> forClass) {
        return (Registry<BiFunction<? super C, Config, ? extends T>>) registryMap.computeIfAbsent(forClass,
                t -> new Registry(forClass.getSimpleName()));
    }

}
