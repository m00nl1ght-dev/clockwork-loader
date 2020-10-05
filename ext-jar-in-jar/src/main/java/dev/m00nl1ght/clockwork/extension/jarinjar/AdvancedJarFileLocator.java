package dev.m00nl1ght.clockwork.extension.jarinjar;

import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.locator.AbstractCachedLocator;
import dev.m00nl1ght.clockwork.locator.LocatorConfig;
import dev.m00nl1ght.clockwork.locator.PluginLocatorType;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.util.Arguments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class AdvancedJarFileLocator extends AbstractCachedLocator {

    // TODO refactor

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String LIBS_DIR = "libs";

    public static final String NAME = "AdvancedJarFileLocator";
    public static final PluginLocatorType FACTORY = AdvancedJarFileLocator::new;

    protected final File lookupPath;
    protected final JarInJarPolicy jarInJarPolicy;

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
        return newConfig(name, path, JarInJarPolicy.DENY, readers, false);
    }

    public static LocatorConfig newConfig(String name, File path, JarInJarPolicy policy, Set<String> readers, boolean wildcard) {
        return new LocatorConfig(name, NAME, Map.of("path", path.getPath(), "jarInJarPolicy", policy.name()), readers, wildcard);
    }

    /**
     * Constructs a new PluginLocator that can find plugins saved as .jar files.
     */
    protected AdvancedJarFileLocator(LocatorConfig config, Set<PluginReader> readers) {
        super(config, readers);
        this.lookupPath = new File(config.get("path"));
        this.jarInJarPolicy = config.getEnumOrDefault("jarInJarPolicy", JarInJarPolicy.class, JarInJarPolicy.DENY);
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

    private PluginDescriptor scanFile(Path path, Consumer<PluginReference> pluginConsumer) {
        if (path.getFileName().toString().toLowerCase().endsWith(".jar")) {
            try {
                final var descriptor = tryAllReaders(path);
                if (descriptor == null) return null;
                if (pluginConsumer != null) scanFile(path, descriptor, pluginConsumer);
                return descriptor;
            } catch (PluginLoadingException e) {
                throw e;
            } catch (Exception e) {
                throw PluginLoadingException.generic("Failed to read jar file []", e, path);
            }
        } else {
            return null;
        }
    }

    private void scanFile(Path path, PluginDescriptor descriptor, Consumer<PluginReference> pluginConsumer) {
        final var moduleFinder = ModuleFinder.of(path);
        final var modules = moduleFinder.findAll().iterator();
        if (!modules.hasNext()) {
            LOGGER.warn(this + " found plugin, but no java module in file [" + path + "], ignoring");
            return;
        }

        final var builder = PluginReference.builder(descriptor);
        final var mainModuleName = modules.next().descriptor().name();
        if (modules.hasNext()) throw PluginLoadingException.multipleModulesFound(this, path);

        final var libDir = path.resolve(LIBS_DIR).toFile();
        final var libs = libDir.listFiles();
        if (libs == null) {
            builder.moduleFinder(moduleFinder);
        } else {
            final var finders = new ArrayList<ModuleFinder>();
            if (jarInJarPolicy == JarInJarPolicy.DENY) {
                LOGGER.warn(this + " found libs dir in jar file [" + path + "], but jar-in-jar loading is disabled, ignoring");
            } else {
                for (var file : libs) {
                    final var libPlugin = scanFile(file.toPath(), null);
                    if (libPlugin == null) {
                        final var libModuleFinder = ModuleFinder.of(file.toPath());
                        final var libModules = libModuleFinder.findAll();
                        if (libModules.isEmpty()) {
                            LOGGER.warn(this + " found jar-in-jar library file [" + file + "], but it does not contain any modules, ignoring");
                        } else {
                            finders.add(libModuleFinder);
                        }
                    } else if (jarInJarPolicy == JarInJarPolicy.LIBS_ONLY) {
                        LOGGER.warn(this + " found jar-in-jar plugin [" + libPlugin + "] in [" + file + "], but nested plugin loading is disabled, ignoring");
                    } else {
                        scanFile(file.toPath(), libPlugin, pluginConsumer);
                    }
                }
            }
            builder.moduleFinder(compose(finders, moduleFinder));
        }

        builder.mainModule(mainModuleName);
        builder.locator(this);
        pluginConsumer.accept(builder.build());
    }

    private ModuleFinder compose(List<ModuleFinder> list, ModuleFinder base) {
        if (list.isEmpty()) {
            return base;
        } else {
            list.add(base);
            return ModuleFinder.compose(list.toArray(ModuleFinder[]::new));
        }
    }

    public String toString() {
        return super.toString() + "[" + lookupPath.getPath() + "]";
    }

    public enum JarInJarPolicy {
        DENY, LIBS_ONLY, ALLOW
    }

}
