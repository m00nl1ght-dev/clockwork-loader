package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.core.PluginReference;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;

public class ExplodedDirectoryLocator extends AbstractCachedLocator {

    private static final Logger LOGGER = LogManager.getLogger();

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
    protected void scan(Consumer<PluginReference> pluginConsumer) {
        if (lookupPath.isDirectory() && scanDir(lookupPath.toPath(), pluginConsumer)) return;
        final var list = lookupPath.listFiles();
        if (list != null) Arrays.stream(list).filter(File::isDirectory).forEach(d -> scanDir(d.toPath(), pluginConsumer));
    }

    private boolean scanDir(Path path, Consumer<PluginReference> pluginConsumer) {
        final var pluginInfo = PluginInfoFile.loadFromDir(path);
        if (pluginInfo == null) return false;
        final var builder = pluginInfo.populatePluginBuilder();
        final var moduleFinder = ModuleFinder.of(path);
        final var modules = moduleFinder.findAll().iterator();
        if (!modules.hasNext()) {
            LOGGER.debug(getName() + " found plugin.toml, but no java module in dir [" + path + "], ignoring");
            return true;
        }

        builder.locator(this);
        builder.moduleFinder(moduleFinder);
        builder.mainModule(modules.next().descriptor().name());
        if (modules.hasNext()) throw PluginLoadingException.multipleModulesFound(this, path);
        pluginConsumer.accept(builder.build());
        return true;
    }

    @Override
    public String getName() {
        return "ExplodedDirectoryLocator[" + lookupPath.getPath() + "]";
    }

}
