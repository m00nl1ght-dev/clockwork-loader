package dev.m00nl1ght.clockwork.utils.profiler;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class ProfilerEntry {

    protected final String name;

    protected ProfilerEntry(@NotNull String name) {
        this.name = Objects.requireNonNull(name);
    }

    public @NotNull String getName() {
        return name;
    }

    public abstract void put(long value);

    public abstract void get(long[] dest);

    public abstract void clear();

    public abstract int getCapacity();

    public abstract int getSize();

    public abstract int getCount();

    public abstract long getAverage();

    @Override
    public String toString() {
        return getName() + " (" + getCount() + ") -> " + getAverage();
    }

}
