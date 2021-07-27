package dev.m00nl1ght.clockwork.extension.mixin;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.MainComponent;
import dev.m00nl1ght.clockwork.loader.ClockworkExtension;
import dev.m00nl1ght.clockwork.loader.ExtensionContext;
import dev.m00nl1ght.clockwork.logger.Logger;
import org.jetbrains.annotations.NotNull;

public final class CWLMixinExtension extends MainComponent implements ClockworkExtension {

    static final Logger LOGGER = Logger.create("Clockwork-Ext-Mixin");

    public CWLMixinExtension(@NotNull ClockworkCore core) {
        super(core);
    }

    @Override
    public void registerFeatures(@NotNull ExtensionContext extensionContext) {
        MixinClassTransformer.registerTo(extensionContext.getTransformerRegistry());
    }

}
