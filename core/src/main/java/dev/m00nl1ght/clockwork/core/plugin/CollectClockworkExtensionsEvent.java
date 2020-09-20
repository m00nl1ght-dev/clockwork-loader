package dev.m00nl1ght.clockwork.core.plugin;

import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.core.PluginProcessor;
import dev.m00nl1ght.clockwork.events.impl.ContextAwareEvent;
import dev.m00nl1ght.clockwork.util.Arguments;

public final class CollectClockworkExtensionsEvent extends ContextAwareEvent {

    private final ClockworkLoader loader;

    public CollectClockworkExtensionsEvent(ClockworkLoader loader) {
        this.loader = Arguments.notNull(loader, "loader");
    }

    public void registerPluginProcessor(String id, PluginProcessor pluginProcessor) {
        checkModificationAllowed();
        loader.registerPluginProcessor(id, pluginProcessor);
    }

}
