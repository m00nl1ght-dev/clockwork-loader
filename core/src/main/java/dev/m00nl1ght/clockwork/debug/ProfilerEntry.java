package dev.m00nl1ght.clockwork.debug;

public abstract class ProfilerEntry extends DebugInfo {

    private final ProfilerGroup group;
    private final String name;
    private long lastSTM = -1L;

    protected ProfilerEntry(ProfilerGroup group, String name) {
        this.group = group;
        this.name = group == null ? name : group.entryAdded(this, name);
    }

    public String getName() {
        return name;
    }

    public ProfilerGroup getGroup() {
        return group;
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

    public String print() {
        return name + " -> " + getCount() + "/" + getSize() + " avg " + getAverage() + " min " + getMin() + " max " + getMax();
    }

}
