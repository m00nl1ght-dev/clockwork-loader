package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.core.PluginReference;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.util.function.Consumer;

public class BootLayerLocator extends AbstractCachedLocator {

    private static final Logger LOGGER = LogManager.getLogger();

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
            final var pluginInfo = PluginInfoFile.loadFromDir(path);
            if (pluginInfo == null) return;
            final var builder = pluginInfo.populatePluginBuilder();
            builder.mainModule(moduleName);
            builder.locator(this);
            pluginConsumer.accept(builder.build());
        } catch (PluginLoadingException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.debug("Failed to locate plugin from boot layer module [" + moduleName + "]", e);
        }
    }

    @Override
    public String getName() {
        return "BootLayerLocator";
    }

}
