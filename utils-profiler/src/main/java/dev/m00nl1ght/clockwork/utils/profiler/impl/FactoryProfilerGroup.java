package dev.m00nl1ght.clockwork.utils.profiler.impl;

import dev.m00nl1ght.clockwork.utils.profiler.ProfilerEntry;
import dev.m00nl1ght.clockwork.utils.profiler.ProfilerGroup;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class FactoryProfilerGroup extends SimpleProfilerGroup {

    private final Function<String, ProfilerEntry> entryFactory;
    private final Function<String, ProfilerGroup> subgroupFactory;

    public FactoryProfilerGroup(@NotNull String name) {
        this(name, SimpleProfilerEntry::new, FactoryProfilerGroup::new);
    }

    public FactoryProfilerGroup(@NotNull String name,
                                @NotNull Function<String, ProfilerEntry> entryFactory,
                                @NotNull Function<String, ProfilerGroup> subgroupFactory) {
        super(name);
        this.entryFactory = Objects.requireNonNull(entryFactory);
        this.subgroupFactory = Objects.requireNonNull(subgroupFactory);
    }

    @Override
    public ProfilerEntry getEntry(@NotNull String name) {
        return entries.computeIfAbsent(Objects.requireNonNull(name), entryFactory);
    }

    @Override
    public ProfilerGroup getSubgroup(@NotNull String name) {
        return subgroups.computeIfAbsent(Objects.requireNonNull(name), subgroupFactory);
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
