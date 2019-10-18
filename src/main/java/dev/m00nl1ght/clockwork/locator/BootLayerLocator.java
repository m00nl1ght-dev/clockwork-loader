package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.api.PluginLocator;
import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.util.PluginLoadingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class BootLayerLocator implements PluginLocator {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String RES_KEY = PluginInfoFile.INFO_FILE_DIR + "/" + PluginInfoFile.INFO_FILE_NAME;

    @Override
    public void load(Consumer<PluginDefinition> pluginConsumer) {
        try {
            final var res = ClockworkCore.class.getClassLoader().getResources(RES_KEY);
            res.asIterator().forEachRemaining(r -> load(r, pluginConsumer));
        } catch (IOException e) {
            throw PluginLoadingException.generic("Failed to locate plugins from boot layer", e);
        }
    }

    private void load(URL infoRes, Consumer<PluginDefinition> pluginConsumer) {
        try {
            final var infoPath = Paths.get(infoRes.toURI());
            final var pluginInfo = PluginInfoFile.load(infoPath);
            final var builder = pluginInfo.populatePluginBuilder();
            final var plugin = builder.build();
            pluginInfo.populateComponents(plugin);
            pluginInfo.populateTargets(plugin);
            pluginConsumer.accept(plugin);
        } catch (PluginLoadingException e) {
            throw e;
        } catch (Exception e) {
            throw PluginLoadingException.generic("Failed to locate plugin from boot layer: " + infoRes, e);
        }
    }

    @Override
    public String getName() {
        return "BootLayerLocator";
    }

}
