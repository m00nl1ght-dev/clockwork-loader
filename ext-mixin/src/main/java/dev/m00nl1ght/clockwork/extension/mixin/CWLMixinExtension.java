package dev.m00nl1ght.clockwork.extension.mixin;

import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.LoaderExtension;
import dev.m00nl1ght.clockwork.logger.Logger;
import org.jetbrains.annotations.NotNull;

public final class CWLMixinExtension extends LoaderExtension {

    static final Logger LOGGER = Logger.create("Clockwork-Ext-Mixin");

    public CWLMixinExtension(@NotNull ClockworkLoader loader) {
        super(loader);
        // MixinClassTransformer.registerTo(extensionContext); // TODO
    }

}
