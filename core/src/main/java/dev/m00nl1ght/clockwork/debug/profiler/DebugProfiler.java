package dev.m00nl1ght.clockwork.debug.profiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebugProfiler {

    protected final Map<String, ProfilerGroup> groups = new HashMap<>();

    public void addGroup(ProfilerGroup group) {
        final var existing = groups.putIfAbsent(group.getName(), group);
        if (existing != null) throw new IllegalArgumentException("group name duplicate");
    }

    public void addGroups(ProfilerGroup... groups) {
        for (var group : groups) addGroup(group);
    }

    public List<ProfilerGroup> getGroups() {
        return List.copyOf(groups.values());
    }

}
