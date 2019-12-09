package dev.m00nl1ght.clockwork.debug.profiler;

public abstract class ProfilerEntry {

    private final String name;

    protected ProfilerEntry(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void put(int value);

    public void put(long value) {
        if (value > Integer.MAX_VALUE) value = Integer.MAX_VALUE;
        put((int) value);
    }

    public abstract int get(int idx);

    public abstract int getSize();

    public abstract int getCount();

    public abstract int getAverage();

    public abstract int getMax();

    public abstract int getMin();

    @Override
    public String toString() {
        return name + "(" + getCount() + ") => AVG ~ " + getAverage() + " MIN " + getMin() + " MAX " + getMax();
    }

}
