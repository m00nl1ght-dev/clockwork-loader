package dev.m00nl1ght.clockwork.utils.config;

import dev.m00nl1ght.clockwork.utils.config.ConfigSpec.Entry;
import dev.m00nl1ght.clockwork.utils.config.impl.ReadonlyWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface ModifiableConfig extends Config {

    @Nullable ModifiableConfig getModifiableSubconfig(@NotNull String key);

    @Nullable List<? extends ModifiableConfig> getModifiableSubconfigs(@NotNull String key);

    @NotNull ModifiableConfig putString(@NotNull String key, @Nullable String value);

    @NotNull ModifiableConfig putSubconfig(@NotNull String key, @Nullable Config value);

    @NotNull ModifiableConfig putStrings(@NotNull String key, @Nullable Collection<String> value);

    @NotNull ModifiableConfig putSubconfigs(@NotNull String key, @Nullable Collection<? extends Config> value);

    default @NotNull <T> ModifiableConfig put(@NotNull String key, @NotNull Type<T> valueType, @Nullable T value) {
        if (value != null) valueType.put(this, key, value);
        else putString(key, null);
        return this;
    }

    default @NotNull <T> ModifiableConfig put(@NotNull Entry<T> entry, @Nullable T value) {
        if (value != null) entry.type.put(this, entry.key, value);
        else putString(entry.key, null);
        return this;
    }

    @Override
    default @NotNull Config asReadonly() {
        return new ReadonlyWrapper(this);
    }

}
