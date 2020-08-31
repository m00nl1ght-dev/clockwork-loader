package dev.m00nl1ght.clockwork.debug.profiler;

public class SimpleProfilerEntry extends ProfilerEntry {

    private int count = 0;
    private int min = Integer.MAX_VALUE;
    private int max = Integer.MIN_VALUE;
    private long total = 0;

    public SimpleProfilerEntry(String name) {
        super(name);
    }

    @Override
    public void put(int value) {
        if (value < min) min = value;
        if (value > max) max = value;
        total += value;
        count++;
    }

    @Override
    public int get(int idx) {
        throw new UnsupportedOperationException();
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
    public int getAverage() {
        return count == 0 ? 0 : (int) total / count;
    }

    @Override
    public int getMax() {
        return count == 0 ? 0 : max;
    }

    @Override
    public int getMin() {
        return count == 0 ? 0 : min;
    }

}
