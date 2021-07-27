package dev.m00nl1ght.clockwork.extension.mixin;

import dev.m00nl1ght.clockwork.loader.classloading.ClassTransformer;
import dev.m00nl1ght.clockwork.util.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MixinClassTransformer implements ClassTransformer {

    public static final String NAME = "extension.mixin.transformer";

    public static void registerTo(@NotNull Registry<ClassTransformer> registry) {
        Objects.requireNonNull(registry).register(NAME, new MixinClassTransformer());
    }

    @Override
    public byte[] transform(String className, byte[] classBytes) {
        CWLMixinExtension.LOGGER.debug("Transforming: " + className);
        return classBytes;
    }

    @Override
    public int priority() {
        return 0;
    }

}
