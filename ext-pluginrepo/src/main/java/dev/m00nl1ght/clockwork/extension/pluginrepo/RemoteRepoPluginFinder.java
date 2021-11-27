package dev.m00nl1ght.clockwork.extension.pluginrepo;

import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.impl.AbstractIndexedPluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.impl.AbstractPluginFinder;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;
import dev.m00nl1ght.clockwork.utils.logger.FormatUtil;
import dev.m00nl1ght.clockwork.utils.version.Version;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static dev.m00nl1ght.clockwork.utils.config.ConfigValue.*;

public class RemoteRepoPluginFinder extends AbstractIndexedPluginFinder {

    public static final String TYPE = "extension.pluginfinder.remoterepo";
    public static final Spec SPEC = new Spec();

    public static void registerTo(ClockworkLoader loader) {
        loader.getFeatureProviders().register(PluginFinder.class, TYPE, RemoteRepoPluginFinder::new);
    }

    public static ModifiableConfig newConfig(String name, URL rootURL, Path cachePath, boolean wildcard) {
        return newConfig(name, rootURL, cachePath, null, wildcard);
    }

    public static ModifiableConfig newConfig(String name, URL rootURL, Path cachePath, List<String> readers, boolean wildcard) {
        return Config.newConfig(SPEC)
                .put(SPEC.FEATURE_TYPE, TYPE)
                .put(SPEC.FEATURE_NAME, Objects.requireNonNull(name))
                .put(SPEC.READERS, readers)
                .put(SPEC.WILDCARD, wildcard)
                .put(SPEC.ROOT_URL, rootURL.toString())
                .put(SPEC.CACHE_PATH, cachePath);
    }

    private static final int MAX_META_SIZE = 1024 * 1024;

    protected final String rootURL;
    protected final LocalRepoPluginFinder localCache;

    protected RemoteRepoPluginFinder(ClockworkLoader loader, Config config) {
        super(loader, config);
        this.rootURL = config.get(SPEC.ROOT_URL);
        final var cachePath = config.get(SPEC.CACHE_PATH);
        final var cacheConfig = LocalRepoPluginFinder.newConfig("localCache", cachePath, readerNames, wildcard);
        this.localCache = new LocalRepoPluginFinder(loader, cacheConfig);
    }

    @Override
    protected Set<String> indexPlugins(ClockworkLoader loader) {
        final var meta = downloadMeta("pluginrepo.index");
        return Arrays.stream(meta.split("\n"))
                .map(String::strip)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected Set<Version> indexVersions(ClockworkLoader loader, String pluginId) {
        final var meta = downloadMeta(pluginId + "/versions.index");
        return Arrays.stream(meta.split("\n"))
                .map(String::strip).map(Version::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected Optional<PluginReference> find(ClockworkLoader loader, Collection<PluginReader> readers, String pluginId, Version version) {
        final var fromCache = localCache.find(loader, pluginId, version);
        if (fromCache.isPresent()) return fromCache;
        downloadToCache(pluginId, version);
        return localCache.find(loader, readers, pluginId, version);
    }

    protected void downloadToCache(String pluginId, Version version) {
        try {
            final var loc = pluginId + "/" + version + "/" + LocalRepoPluginFinder.JAR_FILE;
            final var cacheTarget = localCache.getRootPath().resolve(loc);
            final var url = new URL(rootURL + "/" + loc);
            try (final var byteChannel = Channels.newChannel(url.openStream());
                 final var fileOutputStream = new FileOutputStream(cacheTarget.toFile())) {
                fileOutputStream.getChannel().transferFrom(byteChannel, 0, Long.MAX_VALUE);
            }
        } catch (Exception e) {
            throw FormatUtil.rtExc(e, "Failed to download plugin [] version [] from []", pluginId, version, rootURL);
        }
    }

    protected String downloadMeta(String location) {
        try (final var in = new URL(rootURL + "/" + location).openStream()) {
            final var str = new String(in.readNBytes(MAX_META_SIZE), StandardCharsets.UTF_8);
            if (str.length() >= MAX_META_SIZE) throw new IllegalStateException("max metadata size reached");
            return str;
        } catch (Exception e) {
            throw FormatUtil.rtExc(e, "Failed to download metadata [] from []", location, rootURL);
        }
    }

    public LocalRepoPluginFinder getLocalCache() {
        return localCache;
    }

    public String getRootURL() {
        return rootURL;
    }

    @Override
    public String toString() {
        return TYPE + "[" + name +  "]";
    }

    public static class Spec extends AbstractPluginFinder.Spec {

        public final Entry<String>          ROOT_URL    = entry("rootURL", T_STRING).required();
        public final Entry<Path>            CACHE_PATH  = entry("cachePath", T_PATH).required();

        protected Spec(String specName) {
            super(specName);
        }

        private Spec() {
            super(TYPE);
            initialize();
        }

    }

}
