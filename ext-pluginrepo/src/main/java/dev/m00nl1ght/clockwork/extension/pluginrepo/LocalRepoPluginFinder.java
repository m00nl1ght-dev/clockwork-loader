package dev.m00nl1ght.clockwork.extension.pluginrepo;

import dev.m00nl1ght.clockwork.core.ClockworkException;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.impl.AbstractIndexedPluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.impl.AbstractPluginFinder;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.loader.reader.impl.PluginReaderUtil;
import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;
import dev.m00nl1ght.clockwork.utils.version.Version;

import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static dev.m00nl1ght.clockwork.utils.config.ConfigValue.*;

public class LocalRepoPluginFinder extends AbstractIndexedPluginFinder {

    public static final String TYPE = "extension.pluginfinder.localrepo";
    public static final Spec SPEC = new Spec();

    public static void registerTo(ClockworkLoader loader) {
        loader.getFeatureProviders().register(PluginFinder.class, TYPE, LocalRepoPluginFinder::new);
    }

    public static ModifiableConfig newConfig(String name, Path rootPath, boolean wildcard) {
        return newConfig(name, rootPath, null, wildcard);
    }

    public static ModifiableConfig newConfig(String name, Path rootPath, List<String> readers, boolean wildcard) {
        return Config.newConfig(SPEC)
                .put(SPEC.FEATURE_TYPE, TYPE)
                .put(SPEC.FEATURE_NAME, Objects.requireNonNull(name))
                .put(SPEC.READERS, readers)
                .put(SPEC.WILDCARD, wildcard)
                .put(SPEC.ROOT_PATH, rootPath);
    }

    static final String JAR_FILE = "plugin.jar";

    protected final Path rootPath;

    protected LocalRepoPluginFinder(ClockworkLoader loader, Config config) {
        super(loader, config);
        this.rootPath = config.get(SPEC.ROOT_PATH);
    }

    @Override
    protected Set<String> indexPlugins(ClockworkLoader loader) {
        try {
            return Files.list(rootPath).filter(Files::isDirectory)
                    .map(Path::getFileName).map(Path::toString)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (Exception e) {
            throw ClockworkException.generic(e, "Failed to index plugins");
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
            throw ClockworkException.generic(e, "Failed to index plugin versions for []", pluginId);
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
            throw ClockworkException.generic(e, "Failed to read plugin [] version []", pluginId, version);
        }
    }

    public Path getRootPath() {
        return rootPath;
    }

    @Override
    public String toString() {
        return TYPE + "[" + name +  "]";
    }

    public static class Spec extends AbstractPluginFinder.Spec {

        public final Entry<Path> ROOT_PATH = entry("rootPath", T_PATH).required();

        protected Spec(String specName) {
            super(specName);
        }

        private Spec() {
            super(TYPE);
            initialize();
        }

    }

}
