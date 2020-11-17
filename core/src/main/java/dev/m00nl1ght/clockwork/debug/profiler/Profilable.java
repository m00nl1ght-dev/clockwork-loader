package dev.m00nl1ght.clockwork.debug.profiler;

import dev.m00nl1ght.clockwork.util.FormatUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public interface Profilable<G extends ProfilerGroup> {

    default void attachProfiler(@NotNull G profilerGroup) {
        if (!supportsProfilers())
            throw FormatUtil.unspExc(this.getClass().getSimpleName() + ": This implementation does not support profilers");
    }

    @NotNull
    default Set<@NotNull ? extends G> attachDefaultProfilers() {
        return Collections.emptySet();
    }

    default void detachAllProfilers() {}

    default boolean supportsProfilers() {
        return false;
    }

}
