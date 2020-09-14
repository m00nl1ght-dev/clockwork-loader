package dev.m00nl1ght.clockwork.test.event;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.events.EventTypeImpl;

import java.io.File;

public class PluginInitEvent extends Event {

    public static final EventType<PluginInitEvent, ClockworkCore> TYPE = new EventTypeImpl<>(PluginInitEvent.class, ClockworkCore.class);

    private final ClockworkCore cwc;
    private final File dataDir;

    public PluginInitEvent(ClockworkCore cwc, File dataDir) {
        this.cwc = cwc;
        this.dataDir = dataDir;
    }

    public ClockworkCore getClockworkCore() {
        return cwc;
    }

    public File getDataDirectory(Object object) {
        final var comp = cwc.getComponentType(object.getClass());
        return comp.map(componentType -> new File(dataDir, componentType.getId()).getAbsoluteFile()).orElse(null);
    }

}
