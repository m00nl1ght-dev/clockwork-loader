package dev.m00nl1ght.clockwork.extension.pluginrepo;

import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.LoaderExtension;
import org.jetbrains.annotations.NotNull;

public final class CWLPluginRepoExtension extends LoaderExtension {

    public CWLPluginRepoExtension(@NotNull ClockworkLoader loader) {
        super(loader);
    }

    @Override
    public void registerFeatures() {
        LocalRepoPluginFinder.registerTo(target);
        RemoteRepoPluginFinder.registerTo(target);
    }

}
