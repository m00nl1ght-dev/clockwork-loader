package dev.m00nl1ght.clockwork.extension.nightconfig;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ClockworkExtension;
import dev.m00nl1ght.clockwork.core.ExtensionContext;
import dev.m00nl1ght.clockwork.core.MainComponent;
import org.jetbrains.annotations.NotNull;

public final class CWLNightconfigExtension extends MainComponent implements ClockworkExtension {

    public CWLNightconfigExtension(@NotNull ClockworkCore core) {
        super(core);
    }

    @Override
    public void registerFeatures(@NotNull ExtensionContext extensionContext) {
        NightconfigPluginReader.registerTo(extensionContext.getReaderTypeRegistry());
    }

}
