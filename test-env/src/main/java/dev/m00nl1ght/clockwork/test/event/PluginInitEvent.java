package dev.m00nl1ght.clockwork.test.event;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.EventType;
import dev.m00nl1ght.clockwork.debug.profiler.core.CoreProfiler;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.test.TestLauncher;

import java.io.File;

public class PluginInitEvent implements Event {

    public static final EventType<PluginInitEvent, ClockworkCore> TYPE = TestLauncher.getCoreTargetType().getEventType(PluginInitEvent.class);

    private final ClockworkCore cwc;
    private final File dataDir;
    private final CoreProfiler profiler;

    public PluginInitEvent(ClockworkCore cwc, File dataDir, CoreProfiler profiler) {
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

    public CoreProfiler getProfiler() {
        return profiler;
    }

}
