package dev.m00nl1ght.clockwork.utils.debug.profiler;

import java.util.List;

public abstract class ProfilerGroup {

    protected final String name;

    public ProfilerGroup(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public List<ProfilerEntry> getEntries() {
        return List.of();
    }

    public List<ProfilerGroup> getGroups() {
        return List.of();
    }

}
