package dev.m00nl1ght.clockwork.component;

import org.jetbrains.annotations.NotNull;

public interface ComponentTarget {

    ComponentContainer getComponentContainer();

    @SuppressWarnings("unchecked")
    static <T extends ComponentTarget> @NotNull TargetType<T> typeOf(@NotNull T target) {
        final var targetType = (TargetType<T>) target.getComponentContainer().getTargetType();
        if (!targetType.getTargetClass().isInstance(target)) throw new RuntimeException();
        return targetType;
    }

}
