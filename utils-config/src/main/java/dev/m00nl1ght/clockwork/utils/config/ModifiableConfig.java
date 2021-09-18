package dev.m00nl1ght.clockwork.utils.config;

import dev.m00nl1ght.clockwork.utils.config.impl.ModifiableConfigImpl;
import dev.m00nl1ght.clockwork.utils.config.impl.ReadonlyWrapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ModifiableConfig extends Config {

    ModifiableConfig getModifiableSubconfigOrNull(String key);

    List<? extends ModifiableConfig> getModifiableSubconfigListOrNull(String key);

    default ModifiableConfig getModifiableSubconfig(String key) {
        final var value = getModifiableSubconfigOrNull(key);
        if (value == null) throw new RuntimeException("Missing subconfig " + key + " in config " + this);
        return value;
    }

    default Optional<? extends ModifiableConfig> getOptionalModifiableSubconfig(String key) {
        return Optional.ofNullable(getModifiableSubconfigOrNull(key));
    }

    default ModifiableConfig getModifiableSubconfigOrEmpty(String key) {
        final var value = getModifiableSubconfigOrNull(key);
        return value == null ? new ModifiableConfigImpl() : value;
    }

    default ModifiableConfig getModifiableSubconfigOrDefault(String key, ModifiableConfig defaultValue) {
        final var value = getModifiableSubconfigOrNull(key);
        return value == null ? defaultValue : value;
    }

    default List<? extends ModifiableConfig> getModifiableSubconfigList(String key) {
        final var value = getModifiableSubconfigListOrNull(key);
        if (value == null) throw new RuntimeException("Missing list " + key + " in config " + this);
        return value;
    }

    default Optional<List<? extends ModifiableConfig>> getOptionalModifiableSubconfigList(String key) {
        return Optional.ofNullable(getModifiableSubconfigListOrNull(key));
    }

    default List<? extends ModifiableConfig> getModifiableSubconfigListOrEmpty(String key) {
        final var value = getModifiableSubconfigListOrNull(key);
        return value == null ? List.of() : value;
    }

    default List<? extends ModifiableConfig> getModifiableSubconfigListOrSingletonOrEmpty(String key) {
        final var list = getModifiableSubconfigListOrNull(key);
        if (list != null) return list;
        final var value = getModifiableSubconfigOrNull(key);
        if (value != null) return List.of(value);
        return List.of();
    }

    ModifiableConfig putString(String key, Object value);

    ModifiableConfig putSubconfig(String key, Config value);

    ModifiableConfig putStrings(String key, Collection<String> value);

    ModifiableConfig putSubconfigs(String key, Collection<? extends Config> value);

    @Override
    default Config asReadonly() {
        return new ReadonlyWrapper(this);
    }

}
