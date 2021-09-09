package dev.m00nl1ght.clockwork.utils.profiler.impl;

import dev.m00nl1ght.clockwork.utils.profiler.ProfilerEntry;
import dev.m00nl1ght.clockwork.utils.profiler.ProfilerGroup;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class SimpleProfilerGroup extends ProfilerGroup {

    protected final Map<String, ProfilerEntry> entries = new LinkedHashMap<>();
    protected final Map<String, ProfilerGroup> subgroups = new LinkedHashMap<>();

    protected final int entryCapacity;

    public SimpleProfilerGroup(@NotNull String name) {
        this(name, 0);
    }

    public SimpleProfilerGroup(@NotNull String name, int entryCapacity) {
        super(name);
        this.entryCapacity = entryCapacity;
        if (entryCapacity < 0) throw new IllegalArgumentException();
    }

    public ProfilerEntry entry(@NotNull String name) {
        return entries.computeIfAbsent(Objects.requireNonNull(name), this::newEntry);
    }

    @SuppressWarnings("unchecked")
    public <T extends ProfilerEntry> T entry(@NotNull String name, @NotNull Function<String, ? extends T> factory) {
        final var ret = (T) entries.computeIfAbsent(Objects.requireNonNull(name), factory);
        if (!ret.getName().equals(name)) throw new IllegalArgumentException();
        return ret;
    }

    public ProfilerGroup subgroup(@NotNull String name) {
        return subgroups.computeIfAbsent(Objects.requireNonNull(name), this::newSubgroup);
    }

    @SuppressWarnings("unchecked")
    public <T extends ProfilerGroup> T subgroup(@NotNull String name, @NotNull Function<String, ? extends T> factory) {
        final var ret = (T) subgroups.computeIfAbsent(Objects.requireNonNull(name), factory);
        if (!ret.getName().equals(name)) throw new IllegalArgumentException();
        return ret;
    }

    public void addEntry(@NotNull ProfilerEntry entry) {
        final var existing = entries.putIfAbsent(entry.getName(), entry);
        if (existing != null) throw new IllegalArgumentException("entry name duplicate: " + entry.getName());
    }

    public void addSubgroup(@NotNull ProfilerGroup group) {
        final var existing = subgroups.putIfAbsent(group.getName(), group);
        if (existing != null) throw new IllegalArgumentException("subgroup name duplicate: " + group.getName());
    }

    protected ProfilerEntry newEntry(@NotNull String name) {
        return entryCapacity > 0 ? new CyclicProfilerEntry(name, entryCapacity) : new SimpleProfilerEntry(name);
    }

    protected ProfilerGroup newSubgroup(@NotNull String name) {
        return new SimpleProfilerGroup(name);
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
