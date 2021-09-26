package dev.m00nl1ght.clockwork.extension.pluginrepo;

import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.impl.AbstractIndexedPluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.impl.AbstractPluginFinder;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.loader.reader.impl.PluginReaderUtil;
import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.Config.Type;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec.Entry;
import dev.m00nl1ght.clockwork.utils.config.ConfiguredFeatures;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;
import dev.m00nl1ght.clockwork.utils.logger.FormatUtil;
import dev.m00nl1ght.clockwork.utils.version.Version;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class LocalRepoPluginFinder extends AbstractIndexedPluginFinder {

    public static final String TYPE = "extension.pluginfinder.localrepo";

    public static final ConfigSpec CONFIG_SPEC = ConfigSpec.create(TYPE, AbstractPluginFinder.CONFIG_SPEC);
    public static final Entry<String> CONFIG_ENTRY_ROOTPATH = CONFIG_SPEC.add("rootPath", Config.STRING).required();
    public static final Type<Config> CONFIG_TYPE = CONFIG_SPEC.buildType();

    public static void registerTo(ClockworkLoader loader) {
        loader.getFeatureProviders().register(PluginFinder.class, TYPE, LocalRepoPluginFinder::new);
    }

    public static ModifiableConfig newConfig(String name, File rootPath, boolean wildcard) {
        return newConfig(name, rootPath, null, wildcard);
    }

    public static ModifiableConfig newConfig(String name, File rootPath, List<String> readers, boolean wildcard) {
        return Config.newConfig(CONFIG_SPEC)
                .put(ConfiguredFeatures.CONFIG_ENTRY_TYPE, TYPE)
                .put(ConfiguredFeatures.CONFIG_ENTRY_NAME, Objects.requireNonNull(name))
                .put(AbstractPluginFinder.CONFIG_ENTRY_READERS, readers)
                .put(AbstractPluginFinder.CONFIG_ENTRY_WILDCARD, wildcard)
                .put(CONFIG_ENTRY_ROOTPATH, rootPath.getPath());
    }

    static final String JAR_FILE = "plugin.jar";

    protected final Path rootPath;

    protected LocalRepoPluginFinder(ClockworkLoader loader, Config config) {
        super(loader, config);
        this.rootPath = Path.of(config.get(CONFIG_ENTRY_ROOTPATH));
    }

    @Override
    protected Set<String> indexPlugins(ClockworkLoader loader) {
        try {
            return Files.list(rootPath).filter(Files::isDirectory)
                    .map(Path::getFileName).map(Path::toString)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (Exception e) {
            throw FormatUtil.rtExc(e, "Failed to index plugins");
        }
    }

    @Override
    protected Set<Version> indexVersions(ClockworkLoader loader, String pluginId) {
        try {
            final var pluginPath = rootPath.resolve(pluginId);
            if (!Files.isDirectory(pluginPath)) return Collections.emptySet();
            return Files.list(pluginPath).filter(Files::isDirectory)
                    .map(Path::getFileName).map(Path::toString)
                    .map(Version::new).collect(Collectors.toUnmodifiableSet());
        } catch (Exception e) {
            throw FormatUtil.rtExc(e, "Failed to index plugin versions for []", pluginId);
        }
    }

    @Override
    protected Optional<PluginReference> find(ClockworkLoader loader, Collection<PluginReader> readers, String pluginId, Version version) {
        try {
            final var pluginPath = rootPath.resolve(pluginId);
            if (!Files.isDirectory(pluginPath)) return Optional.empty();
            final var versionPath = pluginPath.resolve(version.toString());
            if (!Files.isDirectory(versionPath)) return Optional.empty();
            final var jarPath = rootPath.resolve(JAR_FILE);
            if (!Files.isRegularFile(jarPath)) return Optional.empty();
            final var descriptor = PluginReaderUtil.tryRead(readers, jarPath);
            if (descriptor.isEmpty()) return Optional.empty();
            final var jarFinder = ModuleFinder.of(jarPath);
            final var module = jarFinder.findAll().stream().findFirst();
            if (module.isEmpty()) return Optional.empty();
            return Optional.of(PluginReference.of(descriptor.get(), jarFinder, module.get().descriptor().name()));
        } catch (Exception e) {
            throw FormatUtil.rtExc(e, "Failed to read plugin [] version []", pluginId, version);
        }
    }

    public Path getRootPath() {
        return rootPath;
    }

    @Override
    public String toString() {
        return TYPE + "[" + name +  "]";
    }

}
