package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import dev.m00nl1ght.clockwork.event.EventAnnotationProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
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
            for (var file : list) scanFile(file.toPath(), pluginConsumer, false);
        } else {
            scanFile(lookupPath.toPath(), pluginConsumer, false);
        }
    }

    private boolean scanFile(Path path, Consumer<PluginDefinition> pluginConsumer, boolean nested) {
        if (!path.getFileName().toString().toLowerCase().endsWith(".jar")) return false;
        try {
            final var pluginInfo = PluginInfoFile.loadFromDir(path);
            if (pluginInfo == null) return false;
            if (nested && jarInJarPolicy == JarInJarPolicy.LIBS_ONLY) {
                LOGGER.warn(getName() + " found jar in jar plugin [" + pluginInfo.getPluginId() + "] in [" + path + "], but nested plugins are not allowed, ignoring");
                return true;
            }

            final var builder = pluginInfo.populatePluginBuilder();
            final var moduleFinder = ModuleFinder.of(path);
            final var modules = moduleFinder.findAll().iterator();
            if (!modules.hasNext()) {
                LOGGER.warn(getName() + " found plugin.toml, but no java module in file [" + path + "], ignoring");
                return true;
            }

            final var mainMN = modules.next().descriptor().name();
            builder.moduleFinder(moduleFinder, mainMN);
            builder.markForProcessor(EventAnnotationProcessor.NAME);
            if (modules.hasNext()) throw PluginLoadingException.multipleModulesFound(this, path);
            final var plugin = builder.build();
            pluginInfo.populateComponents(plugin);
            pluginInfo.populateTargets(plugin);
            pluginConsumer.accept(plugin);
            return true;
        } catch (PluginLoadingException e) {
            throw e;
        } catch (Exception e) {
            throw PluginLoadingException.generic("Failed to read jar file []", e, path);
        }
    }

    private List<ModuleFinder> findJarInJar(Path path, Consumer<PluginDefinition> pluginConsumer, boolean nested) {
        final var libDir = path.resolve(LIBS_DIR).toFile();
        final var libs = libDir.listFiles();
        if (libs != null) for (var file : libs) {
            if (scanFile(file.toPath(), pluginConsumer, true)) continue;
            final var libModuleFinder = ModuleFinder.of(file.toPath());
            final var libModules = libModuleFinder.findAll().iterator();
            if (!libModules.hasNext()) {
                LOGGER.warn(getName() + " found plugin.toml, but no java module in [" + path + "], ignoring");
                continue;
            }

            // TODO look for non-plugin libs (w/o plugin.toml)
        }
        return List.of();
    }

    @Override
    public String getName() {
        return "JarFileLocator[" + lookupPath.getPath() + "]";
    }

    public enum JarInJarPolicy {
        DENY(false), LIBS_ONLY(false), ALLOW(false), ALLOW_RECURSIVE(true);

        private final boolean allowNested;
        JarInJarPolicy(boolean allowNested) {this.allowNested = allowNested;}
        public boolean isAllowed(boolean nested) {return this != DENY && (!nested || allowNested);}
    }

}
