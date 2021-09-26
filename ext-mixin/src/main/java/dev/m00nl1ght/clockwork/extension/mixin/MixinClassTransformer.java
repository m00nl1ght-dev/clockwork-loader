package dev.m00nl1ght.clockwork.extension.mixin;

import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.classloading.ClassTransformer;
import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.Config.Type;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec;
import dev.m00nl1ght.clockwork.utils.config.ConfiguredFeatures;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;

import java.util.Objects;

public class MixinClassTransformer implements ClassTransformer {

    public static final String TYPE = "extension.mixin.transformer";

    public static final ConfigSpec CONFIG_SPEC = ConfigSpec.create(TYPE, ConfiguredFeatures.CONFIG_SPEC);
    public static final Type<Config> CONFIG_TYPE = CONFIG_SPEC.buildType();

    public static void registerTo(ClockworkLoader loader) {
        loader.getFeatureProviders().register(ClassTransformer.class, TYPE, MixinClassTransformer::new);
    }

    public static ModifiableConfig newConfig(String name) {
        return Config.newConfig(CONFIG_SPEC)
                .put(ConfiguredFeatures.CONFIG_ENTRY_TYPE, TYPE)
                .put(ConfiguredFeatures.CONFIG_ENTRY_NAME, Objects.requireNonNull(name));
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
