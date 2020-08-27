package dev.m00nl1ght.clockwork.core.plugin;

import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.processor.PluginProcessor;
import dev.m00nl1ght.clockwork.util.Preconditions;

public final class CollectClockworkExtensionsEvent implements Event {

    private final ClockworkLoader loader;

    public CollectClockworkExtensionsEvent(ClockworkLoader loader) {
        this.loader = Preconditions.notNull(loader, "loader");
    }

    public void registerPluginProcessor(String id, PluginProcessor pluginProcessor) {
        loader.registerPluginProcessor(id, pluginProcessor);
    }

}
