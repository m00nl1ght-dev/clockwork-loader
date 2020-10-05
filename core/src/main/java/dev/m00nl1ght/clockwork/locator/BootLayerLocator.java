package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.util.Arguments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class BootLayerLocator extends AbstractCachedLocator {

    public static final String NAME = "BootLayerLocator";
    public static final PluginLocatorType FACTORY = BootLayerLocator::new;

    private static final Logger LOGGER = LogManager.getLogger();

    public static void registerTo(ClockworkLoader loader) {
        Arguments.notNull(loader, "loader");
        loader.registerLocatorType(NAME, FACTORY);
    }

    public static void registerTo(CollectClockworkExtensionsEvent event) {
        Arguments.notNull(event, "event");
        event.registerLocatorFactory(NAME, FACTORY);
    }

    public static LocatorConfig newConfig(String name) {
        return newConfig(name, false);
    }

    public static LocatorConfig newConfig(String name, boolean wildcard) {
        return newConfig(name, null, wildcard);
    }

    public static LocatorConfig newConfig(String name, Set<String> readers, boolean wildcard) {
        return new LocatorConfig(name, NAME, Map.of(), readers, wildcard);
    }

    protected BootLayerLocator(LocatorConfig config, Set<PluginReader> readers) {
        super(config, readers);
    }

    @Override
    protected void scan(Consumer<PluginReference> pluginConsumer) {
        final var modules = ModuleLayer.boot().configuration().modules();
        for (var module : modules) scan(module.reference(), pluginConsumer);
    }

    private void scan(ModuleReference moduleReference, Consumer<PluginReference> pluginConsumer) {
        final var moduleName = moduleReference.descriptor().name();
        if (moduleReference.location().isEmpty()) return;
        try {
            final var path = Path.of(moduleReference.location().get());
            final var descriptor = tryAllReaders(path);
            if (descriptor != null) {
                final var builder = PluginReference.builder(descriptor);
                builder.mainModule(moduleName);
                builder.locator(this);
                pluginConsumer.accept(builder.build());
            }
        } catch (PluginLoadingException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.debug("Failed to locate plugin from boot layer module [" + moduleName + "]", e);
        }
    }

}
