package dev.m00nl1ght.clockwork.extension.annotations;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public final class EventHandlers {

    private final Map<Class<?>, Set<EventHandlerMethod<?, ?, ?>>> handlers;

    public EventHandlers(Map<Class<?>, Set<EventHandlerMethod<?, ?, ?>>> handlers) {
        this.handlers = Objects.requireNonNull(handlers).entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> Set.copyOf(e.getValue())));
    }

    public @Nullable Set<@NotNull EventHandlerMethod<?, ?, ?>> get(@NotNull Class<?> handlerClass) {
        return handlers.get(Objects.requireNonNull(handlerClass));
    }

    public @NotNull Set<@NotNull EventHandlerMethod<?, ?, ?>> getAll() {
        return handlers.values().stream().flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet());
    }

}
