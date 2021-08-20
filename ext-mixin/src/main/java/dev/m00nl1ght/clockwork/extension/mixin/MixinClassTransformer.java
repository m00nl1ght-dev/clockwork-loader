package dev.m00nl1ght.clockwork.extension.mixin;

import dev.m00nl1ght.clockwork.config.Config;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.classloading.ClassTransformer;

public class MixinClassTransformer implements ClassTransformer {

    public static final String TYPE = "extension.mixin.transformer";

    public static void registerTo(ClockworkLoader loader) {
        loader.getFeatureProviders().register(ClassTransformer.class, TYPE, MixinClassTransformer::new);
    }

    private MixinClassTransformer(ClockworkLoader loader, Config config) {}

    @Override
    public byte[] transform(String className, byte[] classBytes) {
        CWLMixin.LOGGER.debug("Transforming: " + className);
        return classBytes;
    }

    @Override
    public int priority() {
        return 0;
    }

}
