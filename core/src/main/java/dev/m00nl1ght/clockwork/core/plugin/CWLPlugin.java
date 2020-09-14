package dev.m00nl1ght.clockwork.core.plugin;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.events.EventTypeImplExact;

/**
 * The main class for the internal "clockwork" plugin.
 */
public final class CWLPlugin {

    private final ClockworkCore core;
    private final EventType<CollectClockworkExtensionsEvent, ClockworkCore> collectExtensionsEventType;

    private CWLPlugin(ClockworkCore core) {
        this.core = core;
        final var coreTarget = core.getTargetType(ClockworkCore.class).orElseThrow();
        this.collectExtensionsEventType = new EventTypeImplExact<>(CollectClockworkExtensionsEvent.class, coreTarget);
    }

    public EventType<CollectClockworkExtensionsEvent, ClockworkCore> getCollectExtensionsEventType() {
        return collectExtensionsEventType;
    }

}
