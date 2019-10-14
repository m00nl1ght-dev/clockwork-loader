package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.api.PluginLocator;
import dev.m00nl1ght.clockwork.core.PluginDefinition;

import java.io.File;
import java.util.function.Consumer;

public class JarFileLocator implements PluginLocator {

    private final File lookupPath;

    /**
     * Constructs a new PluginLocator that can find plugins saved as .jar files.
     *
     * @param lookupPath The path to a directory or file that should be scanned for plugins
     */
    public JarFileLocator(File lookupPath) {
        this.lookupPath = lookupPath;
    }

    @Override
    public void load(Consumer<PluginDefinition> pluginConsumer) {
        if (lookupPath.isDirectory()) {
            for (File file : lookupPath.listFiles()) {
                scanFile(file, pluginConsumer);
            }
        } else {
            scanFile(lookupPath, pluginConsumer);
        }
    }

    private void scanFile(File file, Consumer<PluginDefinition> pluginConsumer) {
        if (!file.getName().toLowerCase().endsWith(".jar")) return;
        // TODO
    }

    @Override
    public String getName() {
        return "JarFileLocator[" + lookupPath.getPath() + "]";
    }

}
