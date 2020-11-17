package dev.m00nl1ght.clockwork.core;

import org.jetbrains.annotations.NotNull;

public interface ComponentTarget {

    @NotNull TargetType<?> getTargetType();

    Object getComponent(int internalID);

    @SuppressWarnings("unchecked")
    static <T extends ComponentTarget> @NotNull TargetType<T> typeOf(@NotNull T target) {
        return (TargetType<T>) target.getTargetType();
    }

}
