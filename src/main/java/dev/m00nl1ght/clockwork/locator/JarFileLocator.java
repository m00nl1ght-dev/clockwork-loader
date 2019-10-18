package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.api.PluginLocator;
import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.util.PluginLoadingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.FileSystems;
import java.util.function.Consumer;

public class JarFileLocator implements PluginLocator {

    private static final Logger LOGGER = LogManager.getLogger();

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
        try (final var fs = FileSystems.newFileSystem(file.toPath(), this.getClass().getClassLoader())) {
            final var infoPath = fs.getPath(PluginInfoFile.INFO_FILE_DIR, PluginInfoFile.INFO_FILE_NAME);
            final var pluginInfo = PluginInfoFile.load(infoPath);
            final var builder = pluginInfo.populatePluginBuilder();
            final var moduleFinder = ModuleFinder.of(file.toPath());
            final var modules = moduleFinder.findAll().iterator();
            if (!modules.hasNext()) {
                LOGGER.debug(getName() + " found plugin.toml, but no java module in file [" + file + "], ignoring");
                return;
            }

            builder.moduleFinder(moduleFinder, modules.next().descriptor().name());
            if (modules.hasNext()) throw PluginLoadingException.generic(getName() + " found multiple java modules in file [" + file + "]");
            final var plugin = builder.build();
            pluginInfo.populateComponents(plugin);
            pluginInfo.populateTargets(plugin);
            pluginConsumer.accept(plugin);
        } catch (PluginLoadingException e) {
            throw e;
        } catch (Exception e) {
            throw PluginLoadingException.generic("Failed to read jar file " + file, e);
        }
    }

    @Override
    public String getName() {
        return "JarFileLocator[" + lookupPath.getPath() + "]";
    }

}
