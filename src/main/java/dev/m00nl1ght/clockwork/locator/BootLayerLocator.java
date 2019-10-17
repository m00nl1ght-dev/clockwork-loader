package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.api.PluginLocator;
import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.util.PluginLoadingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.function.Consumer;

public class BootLayerLocator implements PluginLocator {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String RES_KEY = PluginInfoFile.INFO_FILE_DIR + "/" + PluginInfoFile.INFO_FILE_NAME;

    @Override
    public void load(Consumer<PluginDefinition> pluginConsumer) {
        ModuleLayer.boot().modules().forEach(m -> load(m, pluginConsumer)); // TODO rework, this wont work
    }

    private void load(Module module, Consumer<PluginDefinition> pluginConsumer) {
        try {
            final var url = module.getClassLoader().getResource(RES_KEY); // TODO rework, this wont work
            if (url == null) return;
            final var infoPath = Paths.get(url.toURI());
            final var pluginInfo = PluginInfoFile.load(infoPath);
            final var builder = pluginInfo.populatePluginBuilder();
            builder.moduleFinder(null, module.getName());
            final var plugin = builder.build();
            pluginInfo.populateComponents(plugin);
            pluginInfo.populateTargets(plugin);
            pluginConsumer.accept(plugin);
        } catch (PluginLoadingException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn("Failed to locate plugin from boot module " + module.getName(), e);
        }
    }

    @Override
    public String getName() {
        return "BootLayerLocator";
    }

}
