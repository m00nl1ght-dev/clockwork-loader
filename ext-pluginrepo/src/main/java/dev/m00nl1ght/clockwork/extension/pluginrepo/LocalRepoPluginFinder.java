package dev.m00nl1ght.clockwork.extension.pluginrepo;

import dev.m00nl1ght.clockwork.config.Config;
import dev.m00nl1ght.clockwork.config.ImmutableConfig;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.impl.AbstractIndexedPluginFinder;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.loader.reader.impl.PluginReaderUtil;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.version.Version;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class LocalRepoPluginFinder extends AbstractIndexedPluginFinder {

    public static final String TYPE = "extension.pluginfinder.localrepo";

    public static void registerTo(ClockworkLoader loader) {
        loader.getFeatureProviders().register(PluginFinder.class, TYPE, LocalRepoPluginFinder::new);
    }

    public static Config newConfig(String name, File rootPath, boolean wildcard) {
        return newConfig(name, rootPath, null, wildcard);
    }

    public static Config newConfig(String name, File rootPath, List<String> readers, boolean wildcard) {
        return ImmutableConfig.builder()
                .putString("type", TYPE)
                .putString("name", name)
                .putStrings("readers", readers)
                .putString("wildcard", wildcard)
                .putString("rootPath", rootPath.getPath())
                .build();
    }

    static final String JAR_FILE = "plugin.jar";

    protected final Path rootPath;

    protected LocalRepoPluginFinder(ClockworkLoader loader, Config config) {
        super(loader, config);
        this.rootPath = Path.of(config.get("rootPath"));
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
