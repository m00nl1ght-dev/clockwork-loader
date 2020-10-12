package dev.m00nl1ght.clockwork.core.plugin;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.events.EventDispatcher;
import dev.m00nl1ght.clockwork.events.impl.ExactEventDispatcherImpl;

/**
 * The main class for the internal "clockwork" plugin.
 */
public final class CWLPlugin {

    private final ClockworkCore core;
    private final EventDispatcher<CollectClockworkExtensionsEvent, ClockworkCore> collectExtensionsEventDispatcher;

    private CWLPlugin(ClockworkCore core) {
        this.core = core;
        final var coreTarget = core.getTargetType(ClockworkCore.class).orElseThrow();
        this.collectExtensionsEventDispatcher = new ExactEventDispatcherImpl<>(CollectClockworkExtensionsEvent.class, coreTarget);
    }

    public EventDispatcher<CollectClockworkExtensionsEvent, ClockworkCore> getCollectExtensionsEventType() {
        return collectExtensionsEventDispatcher;
    }

}
