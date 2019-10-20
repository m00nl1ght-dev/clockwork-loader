package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.util.function.Consumer;

public class BootLayerLocator implements PluginLocator {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void load(Consumer<PluginDefinition> pluginConsumer) {
        final var modules = ModuleLayer.boot().configuration().modules();
        for (var module : modules) load(module.reference(), pluginConsumer);
    }

    private void load(ModuleReference moduleReference, Consumer<PluginDefinition> pluginConsumer) {
        final var moduleName = moduleReference.descriptor().name();
        if (moduleReference.location().isEmpty()) return;
        try {
            final var path = Path.of(moduleReference.location().get());
            final var pluginInfo = PluginInfoFile.loadFromDir(path);
            if (pluginInfo == null) return;
            final var builder = pluginInfo.populatePluginBuilder();
            builder.moduleFinder(null, moduleName);
            final var plugin = builder.build();
            pluginInfo.populateComponents(plugin);
            pluginInfo.populateTargets(plugin);
            pluginConsumer.accept(plugin);
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
