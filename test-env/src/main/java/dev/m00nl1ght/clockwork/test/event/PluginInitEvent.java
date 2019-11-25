package dev.m00nl1ght.clockwork.test.event;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.debug.DebugProfiler;
import dev.m00nl1ght.clockwork.event.Event;

import java.io.File;

public class PluginInitEvent implements Event {

    private final ClockworkCore cwc;
    private final File dataDir;
    private final DebugProfiler profiler;

    public PluginInitEvent(ClockworkCore cwc, File dataDir, DebugProfiler profiler) {
        this.cwc = cwc;
        this.dataDir = dataDir;
        this.profiler = profiler;
    }

    public ClockworkCore getClockworkCore() {
        return cwc;
    }

    public File getDataDirectory(Object object) {
        final var comp = cwc.getComponentType(object.getClass());
        return comp.map(componentType -> new File(dataDir, componentType.getId()).getAbsoluteFile()).orElse(null);
    }

    public DebugProfiler getProfiler() {
        return profiler;
    }

}
