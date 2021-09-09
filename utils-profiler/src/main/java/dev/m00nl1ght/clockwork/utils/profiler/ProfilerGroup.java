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

    public @NotNull List<@NotNull ProfilerEntry> getEntries() {
        return List.of();
    }

    public @NotNull List<@NotNull ProfilerGroup> getSubgroups() {
        return List.of();
    }

}
