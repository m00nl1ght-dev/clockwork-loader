package dev.m00nl1ght.clockwork.extension.nightconfig;

import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.LoaderExtension;
import org.jetbrains.annotations.NotNull;

public final class CWLNightconfigExtension extends LoaderExtension {

    public CWLNightconfigExtension(@NotNull ClockworkLoader loader) {
        super(loader);
        // NightconfigPluginReader.registerTo(extensionContext); // TODO
    }

}
