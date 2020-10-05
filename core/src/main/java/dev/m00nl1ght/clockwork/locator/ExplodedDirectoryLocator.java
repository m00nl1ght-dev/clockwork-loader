package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.util.Arguments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ExplodedDirectoryLocator extends AbstractCachedLocator {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String NAME = "ExplodedDirectoryLocator";
    public static final PluginLocatorType FACTORY = ExplodedDirectoryLocator::new;

    protected final File lookupPath;

    public static void registerTo(ClockworkLoader loader) {
        Arguments.notNull(loader, "loader");
        loader.registerLocatorType(NAME, FACTORY);
    }

    public static void registerTo(CollectClockworkExtensionsEvent event) {
        Arguments.notNull(event, "event");
        event.registerLocatorFactory(NAME, FACTORY);
    }

    public static LocatorConfig newConfig(String name, File path) {
        return newConfig(name, path, null);
    }

    public static LocatorConfig newConfig(String name, File path, Set<String> readers) {
        return newConfig(name, path, readers, false);
    }

    public static LocatorConfig newConfig(String name, File path, Set<String> readers, boolean wildcard) {
        return new LocatorConfig(name, NAME, Map.of("path", path.getPath()), readers, wildcard);
    }

    /**
     * Constructs a new PluginLocator that can find plugins located in a directory.
     */
    protected ExplodedDirectoryLocator(LocatorConfig config, Set<PluginReader> readers) {
        super(config, readers);
        this.lookupPath = new File(config.get("path"));
    }

    @Override
    protected void scan(Consumer<PluginReference> pluginConsumer) {
        if (lookupPath.isDirectory() && scanDir(lookupPath.toPath(), pluginConsumer)) return;
        final var list = lookupPath.listFiles();
        if (list != null) Arrays.stream(list).filter(File::isDirectory).forEach(d -> scanDir(d.toPath(), pluginConsumer));
    }

    private boolean scanDir(Path path, Consumer<PluginReference> pluginConsumer) {
        final var descriptor = tryAllReaders(path);
        if (descriptor == null) return false;
        final var moduleFinder = ModuleFinder.of(path);
        final var modules = moduleFinder.findAll().iterator();
        if (!modules.hasNext()) {
            LOGGER.warn(this + " found plugin, but no java module in dir [" + path + "], ignoring");
            return true;
        }
        final var builder = PluginReference.builder(descriptor);
        builder.locator(this);
        builder.moduleFinder(moduleFinder);
        builder.mainModule(modules.next().descriptor().name());
        if (modules.hasNext()) throw PluginLoadingException.multipleModulesFound(this, path);
        pluginConsumer.accept(builder.build());
        return true;
    }

    public String toString() {
        return super.toString() + "[" + lookupPath.getPath() + "]";
    }

}
