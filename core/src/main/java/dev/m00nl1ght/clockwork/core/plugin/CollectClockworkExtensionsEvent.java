package dev.m00nl1ght.clockwork.core.plugin;

import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.core.PluginProcessor;
import dev.m00nl1ght.clockwork.events.impl.ContextAwareEvent;
import dev.m00nl1ght.clockwork.fnder.PluginFinderType;
import dev.m00nl1ght.clockwork.reader.PluginReaderType;
import dev.m00nl1ght.clockwork.util.Arguments;

public final class CollectClockworkExtensionsEvent extends ContextAwareEvent {

    private final ClockworkLoader loader;

    public CollectClockworkExtensionsEvent(ClockworkLoader loader) {
        this.loader = Arguments.notNull(loader, "loader");
    }

    public void registerReaderType(String id, PluginReaderType readerType) {
        checkModificationAllowed();
        loader.registerReaderType(id, readerType);
    }

    public void registerLocatorFactory(String id, PluginFinderType locatorType) {
        checkModificationAllowed();
        loader.registerFinderType(id, locatorType);
    }

    public void registerProcessor(String id, PluginProcessor processor) {
        checkModificationAllowed();
        loader.registerProcessor(id, processor);
    }

}
