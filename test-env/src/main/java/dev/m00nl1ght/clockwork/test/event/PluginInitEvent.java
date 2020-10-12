package dev.m00nl1ght.clockwork.test.event;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.events.EventDispatcher;
import dev.m00nl1ght.clockwork.events.impl.ContextAwareEvent;
import dev.m00nl1ght.clockwork.test.TestLauncher;

import java.io.File;

public class PluginInitEvent extends ContextAwareEvent {

    public static final EventDispatcher<PluginInitEvent, ClockworkCore> TYPE =
            TestLauncher.eventBus().getEventDispatcher(PluginInitEvent.class, ClockworkCore.class);

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
