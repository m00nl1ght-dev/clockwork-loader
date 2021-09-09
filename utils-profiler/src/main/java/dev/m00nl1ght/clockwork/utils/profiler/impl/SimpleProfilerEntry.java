package dev.m00nl1ght.clockwork.utils.profiler.impl;

import dev.m00nl1ght.clockwork.utils.profiler.ProfilerEntry;
import org.jetbrains.annotations.NotNull;

public class SimpleProfilerEntry extends ProfilerEntry {

    private long total = 0;
    private int count = 0;

    public SimpleProfilerEntry(@NotNull String name) {
        super(name);
    }

    @Override
    public void put(long value) {
        total += value;
        count++;
    }

    @Override
    public void get(long[] dest) {}

    @Override
    public void clear() {
        total = 0;
        count = 0;
    }

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
        return count;
    }

    @Override
    public long getAverage() {
        return count == 0 ? 0 : total / count;
    }

}
