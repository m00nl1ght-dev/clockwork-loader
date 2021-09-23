package dev.m00nl1ght.clockwork.utils.config;

import dev.m00nl1ght.clockwork.utils.config.impl.ReadonlyWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface ModifiableConfig extends Config {

    @Nullable ModifiableConfig getModifiableSubconfig(@NotNull String key);

    @Nullable List<? extends ModifiableConfig> getModifiableSubconfigs(@NotNull String key);

    @NotNull ModifiableConfig putString(@NotNull String key, @Nullable Object value);

    @NotNull ModifiableConfig putSubconfig(@NotNull String key, @Nullable Config value);

    @NotNull ModifiableConfig putStrings(@NotNull String key, @Nullable Collection<String> value);

    @NotNull ModifiableConfig putSubconfigs(@NotNull String key, @Nullable Collection<? extends Config> value);

    @Override
    default @NotNull Config asReadonly() {
        return new ReadonlyWrapper(this);
    }

}
