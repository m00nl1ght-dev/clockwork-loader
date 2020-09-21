package dev.m00nl1ght.clockwork.core.plugin;

import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.core.PluginProcessor;
import dev.m00nl1ght.clockwork.events.impl.ContextAwareEvent;
import dev.m00nl1ght.clockwork.locator.PluginLocatorFactory;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.util.Arguments;

public final class CollectClockworkExtensionsEvent extends ContextAwareEvent {

    private final ClockworkLoader loader;

    public CollectClockworkExtensionsEvent(ClockworkLoader loader) {
        this.loader = Arguments.notNull(loader, "loader");
    }

    public void registerReader(String id, PluginReader reader) {
        checkModificationAllowed();
        loader.registerReader(id, reader);
    }

    public void registerLocatorFactory(String id, PluginLocatorFactory locatorFactory) {
        checkModificationAllowed();
        loader.registerLocatorFactory(id, locatorFactory);
    }

    public void registerProcessor(String id, PluginProcessor processor) {
        checkModificationAllowed();
        loader.registerProcessor(id, processor);
    }

}
