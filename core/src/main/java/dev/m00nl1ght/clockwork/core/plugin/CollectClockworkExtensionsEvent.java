package dev.m00nl1ght.clockwork.core.plugin;

import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.core.PluginProcessor;
import dev.m00nl1ght.clockwork.events.impl.ContextAwareEvent;
import dev.m00nl1ght.clockwork.fnder.PluginFinderType;
import dev.m00nl1ght.clockwork.reader.PluginReaderType;
import dev.m00nl1ght.clockwork.util.Registry;

import java.util.Objects;

public final class CollectClockworkExtensionsEvent extends ContextAwareEvent {

    private final ClockworkLoader loader;

    public CollectClockworkExtensionsEvent(ClockworkLoader loader) {
        this.loader = Objects.requireNonNull(loader);
    }

    public Registry<PluginReaderType> getReaderTypeRegistry() {
        return loader.getReaderTypeRegistry();
    }

    public Registry<PluginFinderType> getFinderTypeRegistry() {
        return loader.getFinderTypeRegistry();
    }

    public Registry<PluginProcessor> getProcessorRegistry() {
        return loader.getProcessorRegistry();
    }

}
