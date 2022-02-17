package dev.m00nl1ght.clockwork.utils.profiler.impl;

import dev.m00nl1ght.clockwork.utils.profiler.ProfilerEntry;
import org.jetbrains.annotations.NotNull;

public class NopProfilerEntry extends ProfilerEntry {

    public NopProfilerEntry(@NotNull String name) {
        super(name);
    }

    @Override
    public void put(long value) {}

    @Override
    public void get(long[] dest) {}

    @Override
    public void clear() {}

    @Override
    public int getCapacity() {
        return 0;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public long getAverage() {
        return 0;
    }

}
