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
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class JarFileLocator extends AbstractCachedLocator {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String NAME = "JarFileLocator";
    public static final PluginLocatorType FACTORY = JarFileLocator::new;

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
     * Constructs a new PluginLocator that can find plugins saved as .jar files.
     */
    protected JarFileLocator(LocatorConfig config, Set<PluginReader> readers) {
        super(config, readers);
        this.lookupPath = new File(config.get("path"));
    }

    @Override
    protected void scan(Consumer<PluginReference> pluginConsumer) {
        final var list = lookupPath.listFiles();
        if (list != null) {
            for (var file : list) scanFile(file.toPath(), pluginConsumer);
        } else {
            scanFile(lookupPath.toPath(), pluginConsumer);
        }
    }

    private void scanFile(Path path, Consumer<PluginReference> pluginConsumer) {
        if (path.getFileName().toString().toLowerCase().endsWith(".jar")) {
            try {
                final var descriptor = tryAllReaders(path);
                if (descriptor == null) return;

                final var moduleFinder = ModuleFinder.of(path);
                final var modules = moduleFinder.findAll().iterator();
                if (!modules.hasNext()) {
                    LOGGER.warn(this + " found plugin, but no java module in file [" + path + "], ignoring");
                    return;
                }

                final var builder = PluginReference.builder(descriptor);
                final var mainModuleName = modules.next().descriptor().name();
                if (modules.hasNext()) throw PluginLoadingException.multipleModulesFound(this, path);

                builder.moduleFinder(moduleFinder);
                builder.mainModule(mainModuleName);
                builder.locator(this);
                pluginConsumer.accept(builder.build());
            } catch (PluginLoadingException e) {
                throw e;
            } catch (Exception e) {
                throw PluginLoadingException.generic("Failed to read jar file []", e, path);
            }
        }
    }

    public String toString() {
        return super.toString() + "[" + lookupPath.getPath() + "]";
    }

}
