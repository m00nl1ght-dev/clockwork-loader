package dev.m00nl1ght.clockwork.test.env;

import java.util.HashSet;
import java.util.Set;

public class TestContext {

    private final Set<String> markers = new HashSet<>();

    public void addMarker(String marker) {
        if (!markers.add(marker)) {
            throw new IllegalStateException("Marker is already present: " + marker);
        }
    }

    public boolean isMarkerPresent(String marker) {
        return markers.contains(marker);
    }

}
