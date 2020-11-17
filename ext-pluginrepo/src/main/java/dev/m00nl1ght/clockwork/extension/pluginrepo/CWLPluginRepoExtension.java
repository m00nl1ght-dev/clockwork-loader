package dev.m00nl1ght.clockwork.extension.pluginrepo;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ClockworkExtension;
import dev.m00nl1ght.clockwork.core.ExtensionContext;
import org.jetbrains.annotations.NotNull;

public final class CWLPluginRepoExtension implements ClockworkExtension {

    private final ClockworkCore core;

    public CWLPluginRepoExtension(@NotNull ClockworkCore core) {
        this.core = core;
    }

    @Override
    public void registerFeatures(@NotNull ExtensionContext extensionContext) {
        LocalRepoPluginFinder.registerTo(extensionContext.getFinderTypeRegistry());
        RemoteRepoPluginFinder.registerTo(extensionContext.getFinderTypeRegistry());
    }

}
