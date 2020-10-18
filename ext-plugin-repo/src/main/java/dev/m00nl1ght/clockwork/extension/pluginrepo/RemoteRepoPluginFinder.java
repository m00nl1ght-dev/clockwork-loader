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
import dev.m00nl1ght.clockwork.util.config.ImmutableConfig;
import dev.m00nl1ght.clockwork.util.Registry;
import dev.m00nl1ght.clockwork.version.Version;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class RemoteRepoPluginFinder extends AbstractIndexedPluginFinder {

    public static final String NAME = "extension.pluginfinder.remoterepo";
    public static final PluginFinderType FACTORY = RemoteRepoPluginFinder::new;

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
        return null; // TODO
    }

    @Override
    protected Set<Version> indexVersions(LoadingContext context, String pluginId) {
        return null; // TODO
    }

    @Override
    protected Optional<PluginReference> find(LoadingContext context, Collection<PluginReader> readers, String pluginId, Version version) {
        final var fromCache = localCache.find(context, pluginId, version);
        if (fromCache.isPresent()) return fromCache;
        if (downloadToCache(pluginId, version)) {
            return localCache.find(context, readers, pluginId, version);
        } else {
            return Optional.empty();
        }
    }

    protected boolean downloadToCache(String pluginId, Version version) {
        try {
            final var url = new URL(rootURL + "/" + pluginId + "/" + version + "/" + LocalRepoPluginFinder.JAR_FILE);
            // TODO
            return false;
        } catch (Exception e) {
            throw FormatUtil.rtExc(e, "Failed to download plugin [] version [] from []", pluginId, version, rootURL);
        }
    }

}
