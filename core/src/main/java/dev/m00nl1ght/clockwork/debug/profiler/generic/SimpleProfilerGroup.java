package dev.m00nl1ght.clockwork.debug.profiler.generic;

import dev.m00nl1ght.clockwork.debug.profiler.ProfilerEntry;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleProfilerGroup extends ProfilerGroup {

    protected final Map<String, ProfilerEntry> entries = new HashMap<>();
    protected final Map<String, ProfilerGroup> groups = new HashMap<>();

    public SimpleProfilerGroup(String name) {
        super(name);
    }

    public void addEntry(ProfilerEntry entry) {
        final var existing = entries.putIfAbsent(entry.getName(), entry);
        if (existing != null) throw new IllegalArgumentException("entry name duplicate");
    }

    public void addEntries(ProfilerEntry... entries) {
        for (var entry : entries) addEntry(entry);
    }

    public void addGroup(ProfilerGroup group) {
        final var existing = groups.putIfAbsent(group.getName(), group);
        if (existing != null) throw new IllegalArgumentException("group name duplicate");
    }

    public void addGroups(ProfilerGroup... groups) {
        for (var group : groups) addGroup(group);
    }

    @Override
    public List<ProfilerEntry> getEntries() {
        return List.copyOf(entries.values());
    }

    @Override
    public List<ProfilerGroup> getGroups() {
        return List.copyOf(groups.values());
    }

}
