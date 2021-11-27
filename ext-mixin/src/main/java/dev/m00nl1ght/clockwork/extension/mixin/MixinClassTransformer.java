package dev.m00nl1ght.clockwork.extension.mixin;

import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.classloading.ClassTransformer;
import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ConfiguredFeatures;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;

import java.util.Objects;

public class MixinClassTransformer implements ClassTransformer {

    public static final String TYPE = "extension.mixin.transformer";
    public static final Spec SPEC = new Spec();

    public static void registerTo(ClockworkLoader loader) {
        loader.getFeatureProviders().register(ClassTransformer.class, TYPE, MixinClassTransformer::new);
    }

    public static ModifiableConfig newConfig(String name) {
        return Config.newConfig(SPEC)
                .put(SPEC.FEATURE_TYPE, TYPE)
                .put(SPEC.FEATURE_NAME, Objects.requireNonNull(name));
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

    public static class Spec extends ConfiguredFeatures.Spec {

        protected Spec(String specName) {
            super(specName);
        }

        private Spec() {
            super(TYPE);
            initialize();
        }

    }

}
