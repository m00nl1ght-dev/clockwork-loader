package dev.m00nl1ght.clockwork.utils.profiler.impl;

import dev.m00nl1ght.clockwork.utils.profiler.ProfilerEntry;
import dev.m00nl1ght.clockwork.utils.profiler.ProfilerGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SimpleProfilerGroup extends ProfilerGroup {

    protected final Map<String, ProfilerEntry> entries = new LinkedHashMap<>();
    protected final Map<String, ProfilerGroup> subgroups = new LinkedHashMap<>();

    public SimpleProfilerGroup(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull ProfilerEntry getEntry(String name) {
        final var entry = entries.get(Objects.requireNonNull(name));
        if (entry == null) throw new NoSuchElementException("No profiler entry with name: " + name);
        return entry;
    }

    public @Nullable ProfilerEntry getEntryOrNull(String name) {
        return entries.get(Objects.requireNonNull(name));
    }

    @Override
    public @NotNull ProfilerGroup getSubgroup(String name) {
        final var group = subgroups.get(Objects.requireNonNull(name));
        if (group == null) throw new NoSuchElementException("No profiler subgroup with name: " + name);
        return group;
    }

    public @Nullable ProfilerGroup getSubgroupOrNull(String name) {
        return subgroups.get(Objects.requireNonNull(name));
    }

    public void putEntry(@NotNull ProfilerEntry entry) {
        entries.put(entry.getName(), entry);
    }

    public void putSubgroup(@NotNull ProfilerGroup group) {
        subgroups.put(group.getName(), group);
    }

    @Override
    public @NotNull List<@NotNull ProfilerEntry> getEntries() {
        return List.copyOf(entries.values());
    }

    @Override
    public @NotNull List<@NotNull ProfilerGroup> getSubgroups() {
        return List.copyOf(subgroups.values());
    }

}
