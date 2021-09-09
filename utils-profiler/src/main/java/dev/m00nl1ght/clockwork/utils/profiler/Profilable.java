package dev.m00nl1ght.clockwork.utils.profiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Profilable<G extends ProfilerGroup> {

    default void attachProfiler(@NotNull G profilerGroup) {
        if (!supportsProfilers())
            throw new UnsupportedOperationException("This implementation does not support profilers");
    }

    default @Nullable G attachDefaultProfiler() {
        return null;
    }

    default void detachProfiler() {}

    default boolean supportsProfilers() {
        return false;
    }

}
