package dev.m00nl1ght.clockwork.extension.nightconfig;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ClockworkExtension;
import dev.m00nl1ght.clockwork.core.ExtensionContext;
import org.jetbrains.annotations.NotNull;

public final class CWLNightconfigExtension implements ClockworkExtension {

    private final ClockworkCore core;

    public CWLNightconfigExtension(@NotNull ClockworkCore core) {
        this.core = core;
    }

    @Override
    public void registerFeatures(@NotNull ExtensionContext extensionContext) {
        NightconfigPluginReader.registerTo(extensionContext.getReaderTypeRegistry());
    }

}
