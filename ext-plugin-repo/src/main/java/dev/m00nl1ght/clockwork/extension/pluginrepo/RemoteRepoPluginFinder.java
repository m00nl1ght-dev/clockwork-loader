package dev.m00nl1ght.clockwork.extension.pluginrepo;

import dev.m00nl1ght.clockwork.core.LoadingContext;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.fnder.AbstractIndexedPluginFinder;
import dev.m00nl1ght.clockwork.fnder.PluginFinderConfig;
import dev.m00nl1ght.clockwork.fnder.PluginFinderConfig.Builder;
import dev.m00nl1ght.clockwork.fnder.PluginFinderType;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.Registry;
import dev.m00nl1ght.clockwork.util.config.ImmutableConfig;
import dev.m00nl1ght.clockwork.version.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RemoteRepoPluginFinder extends AbstractIndexedPluginFinder {

    public static final String NAME = "extension.pluginfinder.remoterepo";
    public static final PluginFinderType FACTORY = RemoteRepoPluginFinder::new;

    private static final int MAX_META_SIZE = 1024 * 1024;

    protected final String rootURL;
    protected final LocalRepoPluginFinder localCache;

    public static void registerTo(Registry<PluginFinderType> registry) {
        Arguments.notNull(registry, "registry");
        registry.register(NAME, FACTORY);
    }

    public static Builder configBuilder(String name, URL rootURL, File cachePath) {
        return PluginFinderConfig.builder(name, NAME)
                .withParams(ImmutableConfig.builder()
                        .put("rootURL", rootURL.toString())
                        .put("cachePath", cachePath.getPath())
                        .build());
    }

    protected RemoteRepoPluginFinder(PluginFinderConfig config) {
        super(config);
        this.rootURL = config.getParams().get("rootURL");
        final var cachePath = new File(config.getParams().get("cachePath"));
        final var cacheConfig = LocalRepoPluginFinder.configBuilder("localCache", cachePath)
                .withReaders(config.getReaders()).withVerifiers(config.getVerifiers()).build();
        this.localCache = new LocalRepoPluginFinder(cacheConfig);
    }

    @Override
    protected Set<String> indexPlugins(LoadingContext context) {
        final var meta = downloadMeta("pluginrepo.index");
        return Arrays.stream(meta.split("\n"))
                .map(String::strip)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected Set<Version> indexVersions(LoadingContext context, String pluginId) {
        final var meta = downloadMeta(pluginId + "/versions.index");
        return Arrays.stream(meta.split("\n"))
                .map(String::strip).map(Version::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected Optional<PluginReference> find(LoadingContext context, Collection<PluginReader> readers, String pluginId, Version version) {
        final var fromCache = localCache.find(context, pluginId, version);
        if (fromCache.isPresent()) return fromCache;
        downloadToCache(pluginId, version);
        return localCache.find(context, readers, pluginId, version);
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

}
