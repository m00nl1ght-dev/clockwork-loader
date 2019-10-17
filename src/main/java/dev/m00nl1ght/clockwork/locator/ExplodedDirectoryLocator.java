package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.api.PluginLocator;
import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.util.PluginInfoFile;
import dev.m00nl1ght.clockwork.util.PluginLoadingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.util.Arrays;
import java.util.function.Consumer;

public class ExplodedDirectoryLocator implements PluginLocator {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String INFO_FILE_PATH = "META-INF/plugin.toml";

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
        if (lookupPath.isDirectory() && scanDir(lookupPath, pluginConsumer)) return;
        final var list = lookupPath.listFiles();
        if (list != null) Arrays.stream(list).filter(File::isDirectory).forEach(d -> scanDir(d, pluginConsumer));
    }

    private boolean scanDir(File file, Consumer<PluginDefinition> pluginConsumer) {
        final var infoFile = new File(file, INFO_FILE_PATH);
        if (!file.exists()) return false;
        final var pluginInfo = PluginInfoFile.load(infoFile);
        final var builder = pluginInfo.populatePluginBuilder();
        final var moduleFinder = ModuleFinder.of(file.toPath());
        final var modules = moduleFinder.findAll().iterator();
        if (!modules.hasNext()) {
            LOGGER.debug(getName() + " found plugin.toml, but no java module in dir [" + file + "], ignoring");
            return true;
        }

        builder.moduleFinder(moduleFinder, modules.next().descriptor().name());
        if (modules.hasNext()) throw PluginLoadingException.generic(getName() + " found multiple java modules in directory [" + file + "]");
        final var plugin = builder.build();
        pluginInfo.populateComponents(plugin);
        pluginInfo.populateTargets(plugin);
        pluginConsumer.accept(plugin);
        return true;
    }

    @Override
    public String getName() {
        return "ExplodedDirectoryLocator[" + lookupPath.getPath() + "]";
    }

}
