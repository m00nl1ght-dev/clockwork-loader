package dev.m00nl1ght.clockwork.utils.config.impl;

import dev.m00nl1ght.clockwork.utils.config.SimpleDataParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MinimalSDPConfig extends AbstractSDPConfig {

    protected final String keyPrefix;
    protected final String name;

    protected final Function<? super String, ?> valueProvider;
    protected final Supplier<? extends Collection<?>> keyProvider;

    public MinimalSDPConfig(@NotNull Function<? super String, ?> valueProvider,
                            @Nullable Supplier<? extends Collection<?>> keyProvider,
                            @NotNull SimpleDataParser.Format dataFormat,
                            @NotNull String keyPrefix,
                            @NotNull String name) {

        super(dataFormat);
        this.valueProvider = Objects.requireNonNull(valueProvider);
        this.keyProvider = keyProvider;
        this.keyPrefix = Objects.requireNonNull(keyPrefix);
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public Set<String> getKeys() {
        if (keyProvider == null) return Collections.emptySet();
        return keyPrefix.isEmpty()
                ? keyProvider.get().stream()
                .map(Object::toString)
                .collect(Collectors.toUnmodifiableSet())
                : keyProvider.get().stream()
                .map(Object::toString)
                .filter(k -> k.startsWith(keyPrefix))
                .map(k -> k.substring(keyPrefix.length()))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected String getRaw(String key) {
        final var raw = valueProvider.apply(keyPrefix + key);
        return raw == null ? null : raw.toString();
    }

    @Override
    public String toString() {
        return name;
    }

}
