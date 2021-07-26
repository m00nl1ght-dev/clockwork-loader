package dev.m00nl1ght.clockwork.extension.pluginrepo;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.loader.ClockworkExtension;
import dev.m00nl1ght.clockwork.loader.ExtensionContext;
import dev.m00nl1ght.clockwork.core.MainComponent;
import org.jetbrains.annotations.NotNull;

public final class CWLPluginRepoExtension extends MainComponent implements ClockworkExtension {

    public CWLPluginRepoExtension(@NotNull ClockworkCore core) {
        super(core);
    }

    @Override
    public void registerFeatures(@NotNull ExtensionContext extensionContext) {
        LocalRepoPluginFinder.registerTo(extensionContext.getFinderTypeRegistry());
        RemoteRepoPluginFinder.registerTo(extensionContext.getFinderTypeRegistry());
    }

}
