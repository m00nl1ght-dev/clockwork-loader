package dev.m00nl1ght.clockwork.debug;

import java.util.HashMap;
import java.util.Map;

public class ProfilerGroup extends DebugInfo {

    protected final DebugProfiler profiler;
    protected final String name;
    protected final Map<String, ProfilerEntry> entries = new HashMap<>();

    public ProfilerGroup(DebugProfiler profiler, String name) {
        this.profiler = profiler;
        this.name = profiler.groupAdded(this, name);
    }

    protected synchronized String entryAdded(ProfilerEntry entry, String desiredName) {
        final var name = uniqueEntryName(desiredName);
        entries.put(name, entry);
        return name;
    }

    private String uniqueEntryName(String str) {
        if (entries.get(str) != null) {
            var i = 1; while (entries.get(str + "#" + i) != null) i++;
            return str + "#" + i;
        } else {
            return str;
        }
    }

    public DebugProfiler getProfiler() {
        return profiler;
    }

    public String getName() {
        return name;
    }

    public String print() {
        final var str = new StringBuilder();
        print(str);
        return str.toString();
    }

    @SuppressWarnings("Convert2streamapi")
    protected void print(StringBuilder str) {
        str.append("[").append(name).append("]\n");
        for (var entry : entries.values()) str.append(entry.print()).append("\n");
    }

}
