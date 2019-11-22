package dev.m00nl1ght.clockwork.debug;

import java.util.LinkedList;
import java.util.List;

public class DebugInfo {

    private final List<String> entries = new LinkedList<>();

    public Iterable<String> getDebugInfo() {
        return entries;
    }

    public void addDebugInfo(String data) {
        entries.add(data);
    }

    public void addDebugInfo(String id, String data) {
        entries.add(id + ": " + data);
    }

}
