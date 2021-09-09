package dev.m00nl1ght.clockwork.utils.profiler.impl;

import dev.m00nl1ght.clockwork.utils.profiler.ProfilerEntry;
import dev.m00nl1ght.clockwork.utils.profiler.ProfilerGroup;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class SequencialProfilerGroup extends ProfilerGroup {

    protected final ProfilerEntry[] entries;

    protected int idx = 0;
    protected long timer = 0L;

    public SequencialProfilerGroup(@NotNull String name, ProfilerEntry... entries) {
        this(name, List.of(entries));
    }

    public SequencialProfilerGroup(@NotNull String name, @NotNull Collection<@NotNull ProfilerEntry> entries) {
        super(name);
        this.entries = entries.toArray(ProfilerEntry[]::new);
    }

    @Override
    public @NotNull List<@NotNull ProfilerEntry> getEntries() {
        return List.of(entries);
    }

    public void begin() {
        if (idx != 0) throw new IllegalStateException();
        timer = System.currentTimeMillis();
        idx = 0;
    }

    public void step() {
        final var endTime = System.currentTimeMillis();
        entries[idx].put(endTime - timer);
        timer = System.currentTimeMillis();
        idx++;
    }

    public void end() {
        final var endTime = System.currentTimeMillis();
        if (idx != entries.length - 1) throw new IllegalStateException();
        entries[idx].put(endTime - timer);
        idx = 0;
    }

}
