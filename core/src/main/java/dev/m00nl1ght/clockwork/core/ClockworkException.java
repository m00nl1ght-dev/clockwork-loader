package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.component.ComponentType;
import dev.m00nl1ght.clockwork.component.TargetType;
import dev.m00nl1ght.clockwork.descriptor.Namespaces;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventListener;
import dev.m00nl1ght.clockwork.utils.logger.Logger;
import dev.m00nl1ght.clockwork.utils.reflect.TypeRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ClockworkException extends RuntimeException {

    private final List<String> stack = new LinkedList<>();

    protected ClockworkException(@NotNull String message) {
        super(message);
    }

    protected ClockworkException(@NotNull String message, Throwable cause) {
        super(message, cause);
    }

    public static @NotNull ClockworkException generic(@NotNull String message, Object... objects) {
        return new ClockworkException(Logger.formatMessage(message, objects));
    }

    public static @NotNull ClockworkException generic(Throwable cause, @NotNull String message, Object... objects) {
        return new ClockworkException(Logger.formatMessage(message, objects), cause);
    }

    public static @NotNull ClockworkException generic(@NotNull LoadedPlugin causer, @NotNull String message, Object... objects) {
        return new ClockworkException(Logger.formatMessage(message, objects)).addPluginToStack(causer);
    }

    public static @NotNull ClockworkException generic(@NotNull LoadedPlugin causer, Throwable cause, @NotNull String message, Object... objects) {
        return new ClockworkException(Logger.formatMessage(message, objects), cause).addPluginToStack(causer);
    }

    public static @NotNull ClockworkException generic(@NotNull ComponentType causer, @NotNull String message, Object... objects) {
        return new ClockworkException(Logger.formatMessage(message, objects)).addComponentToStack(causer);
    }

    public static @NotNull ClockworkException generic(@NotNull ComponentType causer, Throwable cause, @NotNull String message, Object... objects) {
        return new ClockworkException(Logger.formatMessage(message, objects), cause).addComponentToStack(causer);
    }

    public static @NotNull IllegalArgumentException illegalArgument(@NotNull String message, Object... objects) {
        return new IllegalArgumentException(Logger.formatMessage(message, objects));
    }

    public static @NotNull ClockworkException inEventListener(@NotNull EventListener<?, ?, ?> listener, @NotNull Event event, Throwable cause) {
        final var message = Logger.formatMessage("Exception thrown in event listener [] while handling event []", listener, event);
        return new ClockworkException(message, cause).addComponentToStack(listener.getComponentType());
    }

    public static @NotNull ClockworkException inComponentInit(@NotNull ComponentType component, Throwable cause) {
        final var message = Logger.formatMessage("Exception thrown while initialising component []", component);
        return new ClockworkException(message, cause).addComponentToStack(component);
    }

    public static @NotNull ClockworkException inComponentInterface(@NotNull ComponentType component, @NotNull TypeRef<?> interfaceType, Throwable cause) {
        final var message = Logger.formatMessage("Exception thrown while applying interface [] for component []", interfaceType, component);
        return new ClockworkException(message, cause).addComponentToStack(component);
    }

    public @NotNull ClockworkException addToStack(@NotNull String id) {
        this.stack.add(Namespaces.combinedId(Objects.requireNonNull(id)));
        return this;
    }

    public @NotNull ClockworkException addPluginToStack(@NotNull LoadedPlugin plugin) {
        this.stack.add(Objects.requireNonNull(plugin).getId());
        return this;
    }

    public @NotNull ClockworkException addComponentToStack(@NotNull ComponentType component) {
        Objects.requireNonNull(component);
        if (component instanceof RegisteredComponentType) {
            final var registered = (RegisteredComponentType) component;
            this.stack.add(registered.getId());
        }
        return this;
    }

    public @NotNull ClockworkException addTargetToStack(@NotNull TargetType<?> target) {
        Objects.requireNonNull(target);
        if (target instanceof RegisteredTargetType<?>) {
            final var registered = (RegisteredTargetType<?>) target;
            this.stack.add(registered.getId());
        }
        return this;
    }

    public @NotNull List<@NotNull String> getStack() {
        return Collections.unmodifiableList(stack);
    }

    public @Nullable String getSource() {
        return stack.isEmpty() ? null : stack.get(0);
    }

}
