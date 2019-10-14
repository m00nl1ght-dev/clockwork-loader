package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.api.PluginLocator;
import dev.m00nl1ght.clockwork.core.PluginDefinition;

import java.io.File;
import java.util.function.Consumer;

public class ExplodedDirectoryLocator implements PluginLocator {

    private final File lookupPath;

    /**
     * Constructs a new PluginLocator that can find plugins located in this directory.
     *
     * @param lookupPath The path to a directory that should be scanned for plugins
     */
    public ExplodedDirectoryLocator(File lookupPath) {
        this.lookupPath = lookupPath;
    }

    @Override
    public void load(Consumer<PluginDefinition> pluginConsumer) {

    }

    private void scanDir(File file, Consumer<PluginDefinition> pluginConsumer) {
        // TODO
    }

    @Override
    public String getName() {
        return "ExplodedDirectoryLocator[" + lookupPath.getPath() + "]";
    }

}
