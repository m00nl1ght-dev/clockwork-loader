package dev.m00nl1ght.clockwork.utils.profiler.impl;

import dev.m00nl1ght.clockwork.utils.profiler.ProfilerEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Not thread-safe.
 */
public class CyclicProfilerEntry extends ProfilerEntry {

    protected final long[] data;
    protected long total = 0;
    protected int count = 0;

    public CyclicProfilerEntry(@NotNull String name, int capacity) {
        super(name);
        this.data = new long[capacity];
    }

    @Override
    public void put(long value) {
        final var pointer = count % data.length;
        total += value;
        total -= data[pointer];
        data[pointer] = value;
        count++;
    }

    @Override
    public void get(long[] dest) {
        System.arraycopy(data, 0, dest, 0, getSize());
    }

    @Override
    public void clear() {
        Arrays.fill(data, 0);
        total = 0;
        count = 0;
    }

    public long getAtIdx(int idx) {
        if (idx < 0 || idx > data.length || idx > count) throw new ArrayIndexOutOfBoundsException();
        return data[count <= data.length ? idx : (count + idx) % data.length];
    }

    @Override
    public int getCapacity() {
        return data.length;
    }

    @Override
    public int getSize() {
        return Math.min(data.length, count);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public long getAverage() {
        return count == 0 ? 0 : total / getSize();
    }

}
