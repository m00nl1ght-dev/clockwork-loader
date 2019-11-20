package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import dev.m00nl1ght.clockwork.event.EventAnnotationProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.function.Consumer;

public class JarFileLocator extends AbstractCachedLocator {

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
    protected void scan(Consumer<PluginDefinition> pluginConsumer) {
        final var list = lookupPath.listFiles();
        if (list != null) {
            for (var file : list) scanFile(file.toPath(), pluginConsumer);
        } else {
            scanFile(lookupPath.toPath(), pluginConsumer);
        }
    }

    private void scanFile(Path path, Consumer<PluginDefinition> pluginConsumer) {
        if (!path.getFileName().toString().toLowerCase().endsWith(".jar")) return;
        try {
            final var pluginInfo = PluginInfoFile.loadFromDir(path);
            if (pluginInfo == null) return;
            final var builder = pluginInfo.populatePluginBuilder();
            final var moduleFinder = ModuleFinder.of(path);
            final var modules = moduleFinder.findAll().iterator();
            if (!modules.hasNext()) {
                LOGGER.debug(getName() + " found plugin.toml, but no java module in file [" + path + "], ignoring");
                return;
            }

            builder.moduleFinder(moduleFinder, modules.next().descriptor().name());
            builder.markForProcessor(EventAnnotationProcessor.NAME);
            if (modules.hasNext()) throw PluginLoadingException.multipleModulesFound(this, path);
            final var plugin = builder.build();
            pluginInfo.populateComponents(plugin);
            pluginInfo.populateTargets(plugin);
            pluginConsumer.accept(plugin);
        } catch (PluginLoadingException e) {
            throw e;
        } catch (Exception e) {
            throw PluginLoadingException.generic("Failed to read jar file []", e, path);
        }
    }

    @Override
    public String getName() {
        return "JarFileLocator[" + lookupPath.getPath() + "]";
    }

}
