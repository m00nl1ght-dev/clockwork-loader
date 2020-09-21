package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class BootLayerLocator extends AbstractCachedLocator {

    public static final String NAME = "BootLayerLocator";
    public static final PluginLocatorFactory FACTORY = BootLayerLocator::new;

    private static final Logger LOGGER = LogManager.getLogger();

    public static LocatorConfig newConfig() {
        return newConfig(false);
    }

    public static LocatorConfig newConfig(boolean wildcard) {
        return newConfig(null, wildcard);
    }

    public static LocatorConfig newConfig(Set<String> readers, boolean wildcard) {
        return new LocatorConfig(NAME, Map.of(), readers, wildcard);
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
            for (final var reader : readers) {
                final var builder = reader.read(path);
                if (builder != null) {
                    builder.mainModule(moduleName);
                    builder.locator(this);
                    pluginConsumer.accept(builder.build());
                    return;
                }
            }
        } catch (PluginLoadingException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.debug("Failed to locate plugin from boot layer module [" + moduleName + "]", e);
        }
    }

}
