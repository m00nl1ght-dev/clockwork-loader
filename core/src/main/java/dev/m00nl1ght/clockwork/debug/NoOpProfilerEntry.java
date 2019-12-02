package dev.m00nl1ght.clockwork.debug;

public class NoOpProfilerEntry extends ProfilerEntry {

    public static final ProfilerEntry INSTANCE = new NoOpProfilerEntry();

    private NoOpProfilerEntry() {
        super(null, "NOOP");
    }

    @Override
    public void start() {

    }

    @Override
    public void end() {

    }

    @Override
    public void put(int value) {

    }

    @Override
    public int get(int idx) {
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
    public int getAverage() {
        return 0;
    }

    @Override
    public int getMax() {
        return 0;
    }

    @Override
    public int getMin() {
        return 0;
    }

}
