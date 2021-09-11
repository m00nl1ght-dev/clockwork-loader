package dev.m00nl1ght.clockwork.utils.profiler;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public abstract class ProfilerGroup {

    protected final String name;

    protected ProfilerGroup(@NotNull String name) {
        this.name = Objects.requireNonNull(name);
    }

    public final @NotNull String getName() {
        return name;
    }

    public abstract @NotNull ProfilerEntry getEntry(String name);

    public abstract @NotNull ProfilerGroup getSubgroup(String name);

    public abstract @NotNull List<@NotNull ProfilerEntry> getEntries();

    public abstract @NotNull List<@NotNull ProfilerGroup> getSubgroups();

    @Override
    public String toString() {
        return name;
    }

}
