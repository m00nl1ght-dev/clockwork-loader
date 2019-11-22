package dev.m00nl1ght.clockwork.debug;

import java.util.HashMap;
import java.util.Map;

public class DebugProfiler {

    protected final Map<String, ProfilerGroup> groups = new HashMap<>();

    protected synchronized String groupAdded(ProfilerGroup group, String desiredName) {
        final var name = uniqueGroupName(desiredName);
        groups.put(name, group);
        return name;
    }

    private String uniqueGroupName(String str) {
        if (groups.get(str) != null) {
            var i = 1; while (groups.get(str + "#" + i) != null) i++;
            return str + "#" + i;
        } else {
            return str;
        }
    }

    public String print() {
        final var str = new StringBuilder();
        str.append("###### Clockwork Loader Debug Info ######\n");
        for (var group : groups.values()) group.print(str);
        str.append("#########################################");
        return str.toString();
    }

}
