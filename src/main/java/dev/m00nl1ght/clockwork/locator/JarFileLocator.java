package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.api.PluginLocator;
import dev.m00nl1ght.clockwork.core.PluginDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.function.Consumer;
import java.util.jar.JarFile;

public class JarFileLocator implements PluginLocator {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String INFO_FILE_PATH = "META-INF/plugin.toml";

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
        final var list = lookupPath.listFiles();
        if (list != null) {
            for (File file : list) scanFile(file, pluginConsumer);
        } else {
            scanFile(lookupPath, pluginConsumer);
        }
    }

    private void scanFile(File file, Consumer<PluginDefinition> pluginConsumer) {
        if (!file.getName().toLowerCase().endsWith(".jar")) return;
        try (final var jarFile = new JarFile(file)) {
            final var entry = jarFile.getEntry(INFO_FILE_PATH);
            if (entry == null) return;
            final var stream = jarFile.getInputStream(entry);
            // TODO
        } catch (Exception e) {
            LOGGER.warn("Failed to read jar file " + file, e);
        }
    }

    @Override
    public String getName() {
        return "JarFileLocator[" + lookupPath.getPath() + "]";
    }

}
