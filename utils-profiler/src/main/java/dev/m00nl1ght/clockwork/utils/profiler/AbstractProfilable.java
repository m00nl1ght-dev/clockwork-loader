package dev.m00nl1ght.clockwork.utils.profiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AbstractProfilable<G extends ProfilerGroup, E extends Enum<E>> implements Profilable<G> {

    private G profiler;
    private ProfilerEntry[] entries;

    private final Class<E> entryEnum;

    protected AbstractProfilable(@NotNull Class<E> entryEnum) {
        this.entryEnum = Objects.requireNonNull(entryEnum);
    }

    public long profilerBegin() {
        return profiler == null ? 0L : System.currentTimeMillis();
    }

    public long profilerNext(@NotNull E entry, long timer) {
        if (profiler == null) return 0L;
        final var now = System.currentTimeMillis();
        entries[entry.ordinal()].put(now - timer);
        return now;
    }

    public void profilerEnd(@NotNull E entry, long timer) {
        if (profiler != null) entries[entry.ordinal()].put(System.currentTimeMillis() - timer);
    }

    @Override
    public void attachProfiler(@NotNull G profilerGroup) {
        this.profiler = Objects.requireNonNull(profilerGroup);
        final var enumValues = entryEnum.getEnumConstants();
        this.entries = new ProfilerEntry[enumValues.length];
        for (int i = 0; i < enumValues.length; i++) {
            this.entries[i] = findProfilerEntry(profiler, enumValues[i]);
        }
    }

    protected @NotNull ProfilerEntry findProfilerEntry(@NotNull G profiler, @NotNull E entry) {
        return profiler.getEntry(entry.name());
    }

    @Override
    public void detachProfiler() {
        this.profiler = null;
        this.entries = null;
    }

    @Override
    public boolean supportsProfilers() {
        return true;
    }

    protected @Nullable G getProfiler() {
        return profiler;
    }

}
