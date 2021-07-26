package dev.m00nl1ght.clockwork.loader.fnder;

import dev.m00nl1ght.clockwork.loader.LoadingContext;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.version.Version;

import java.util.Optional;
import java.util.Set;

public interface PluginFinder {

    Set<String> getAvailablePlugins(LoadingContext context);

    Set<Version> getAvailableVersions(LoadingContext context, String pluginId);

    Optional<PluginReference> find(LoadingContext context, String pluginId, Version version);

}
