package dev.m00nl1ght.clockwork.extension.mixin;

import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.LoaderExtension;
import org.jetbrains.annotations.NotNull;

public final class CWLMixinExtension extends LoaderExtension {

    public CWLMixinExtension(@NotNull ClockworkLoader loader) {
        super(loader);
    }

    @Override
    public void registerFeatures() {
        MixinClassTransformer.registerTo(target);
    }

}
