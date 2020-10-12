package dev.m00nl1ght.clockwork.debug.profiler;

import dev.m00nl1ght.clockwork.util.FormatUtil;

public interface Profilable<G extends ProfilerGroup> {

    default void attachProfiler(G profilerGroup) {
        if (!supportsProfilers())
            throw FormatUtil.unspExc(this.getClass().getSimpleName() + ": This implementation does not support profilers");
    }

    default void detachAllProfilers() {}

    default boolean supportsProfilers() {
        return false;
    }

}
