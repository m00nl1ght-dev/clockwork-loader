package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class JarFileLocator extends AbstractCachedLocator {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String LIBS_DIR = "libs";

    private final File lookupPath;
    private final JarInJarPolicy jarInJarPolicy;

    /**
     * Constructs a new PluginLocator that can find plugins saved as .jar files.
     *
     * @param lookupPath The path to a directory or file that should be scanned for plugins
     * @param jarInJarPolicy Specifies how nested jars should be handled
     */
    public JarFileLocator(File lookupPath, JarInJarPolicy jarInJarPolicy) {
        this.lookupPath = lookupPath;
        this.jarInJarPolicy = jarInJarPolicy;
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

    private PluginInfoFile scanFile(Path path, Consumer<PluginDefinition> pluginConsumer) {
        if (path.getFileName().toString().toLowerCase().endsWith(".jar")) {
            try {
                final var pluginInfo = PluginInfoFile.loadFromDir(path);
                if (pluginInfo == null) return null;
                if (pluginConsumer != null) scanFile(path, pluginInfo, pluginConsumer);
                return pluginInfo;
            } catch (PluginLoadingException e) {
                throw e;
            } catch (Exception e) {
                throw PluginLoadingException.generic("Failed to read jar file []", e, path);
            }
        } else {
            return null;
        }
    }

    private void scanFile(Path path, PluginInfoFile pluginInfo, Consumer<PluginDefinition> pluginConsumer) {
        final var builder = pluginInfo.populatePluginBuilder();
        final var moduleFinder = ModuleFinder.of(path);
        final var modules = moduleFinder.findAll().iterator();
        if (!modules.hasNext()) {
            LOGGER.warn(getName() + " found plugin.toml, but no java module in file [" + path + "], ignoring");
            return;
        }

        final var mainModuleName = modules.next().descriptor().name();
        if (modules.hasNext()) throw PluginLoadingException.multipleModulesFound(this, path);

        final var libDir = path.resolve(LIBS_DIR).toFile();
        final var libs = libDir.listFiles();
        if (libs == null) {
            builder.moduleFinder(moduleFinder, mainModuleName);
        } else {
            final var finders = new ArrayList<ModuleFinder>();
            if (jarInJarPolicy == JarInJarPolicy.DENY) {
                LOGGER.warn(getName() + " found libs dir in jar file [" + path + "], but jar-in-jar loading is disabled, ignoring");
            } else {
                for (var file : libs) {
                    final var libPlugin = scanFile(file.toPath(), null);
                    if (libPlugin == null) {
                        final var libModuleFinder = ModuleFinder.of(file.toPath());
                        final var libModules = libModuleFinder.findAll();
                        if (libModules.isEmpty()) {
                            LOGGER.warn(getName() + " found jar-in-jar library file [" + file + "], but it does not contain any modules, ignoring");
                        } else {
                            finders.add(libModuleFinder);
                        }
                    } else if (jarInJarPolicy == JarInJarPolicy.LIBS_ONLY) {
                        LOGGER.warn(getName() + " found jar-in-jar plugin [" + libPlugin.getPluginId() + "] in [" + file + "], but nested plugin loading is disabled, ignoring");
                    } else {
                        scanFile(file.toPath(), libPlugin, pluginConsumer);
                    }
                }
            }

            builder.moduleFinder(compose(finders, moduleFinder), mainModuleName);
        }

        builder.locator(this);
        final var plugin = builder.build();
        pluginInfo.populateComponents(plugin);
        pluginInfo.populateTargets(plugin);
        pluginConsumer.accept(plugin);
    }

    private ModuleFinder compose(List<ModuleFinder> list, ModuleFinder base) {
        if (list.isEmpty()) {
            return base;
        } else {
            list.add(base);
            return ModuleFinder.compose(list.toArray(ModuleFinder[]::new));
        }
    }

    @Override
    public String getName() {
        return "JarFileLocator[" + lookupPath.getPath() + "]";
    }

    public enum JarInJarPolicy {
        DENY, LIBS_ONLY, ALLOW
    }

}
