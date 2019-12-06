package dev.m00nl1ght.clockwork.debug.profiler;

public abstract class ProfilerEntry {

    private final String name;
    private long lastSTM = -1L;

    protected ProfilerEntry(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void start() {
        if (lastSTM >= 0) throw new IllegalStateException();
        lastSTM = System.nanoTime();
    }

    public void end() {
        if (lastSTM < 0) throw new IllegalStateException();
        var diff = System.nanoTime() - lastSTM;
        if (diff > Integer.MAX_VALUE) diff = Integer.MAX_VALUE;
        put((int) diff);
        lastSTM = -1L;
    }

    public abstract void put(int value);

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
