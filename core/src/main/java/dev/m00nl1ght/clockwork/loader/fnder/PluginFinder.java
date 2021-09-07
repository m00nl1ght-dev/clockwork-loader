package dev.m00nl1ght.clockwork.loader.fnder;

import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.utils.version.Version;

import java.util.Optional;
import java.util.Set;

public interface PluginFinder {

    Set<String> getAvailablePlugins(ClockworkLoader loader);

    Set<Version> getAvailableVersions(ClockworkLoader loader, String pluginId);

    Optional<PluginReference> find(ClockworkLoader loader, String pluginId, Version version);

    boolean isWildcard();

}
