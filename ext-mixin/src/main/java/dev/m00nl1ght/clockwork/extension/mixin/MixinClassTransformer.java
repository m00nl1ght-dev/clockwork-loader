package dev.m00nl1ght.clockwork.extension.mixin;

import dev.m00nl1ght.clockwork.loader.ExtensionContext;
import dev.m00nl1ght.clockwork.loader.classloading.ClassTransformer;

import java.util.Objects;

public class MixinClassTransformer implements ClassTransformer {

    public static final String NAME = "extension.mixin.transformer";

    public static void registerTo(ExtensionContext context) {
        Objects.requireNonNull(context).registryFor(ClassTransformer.class).register(NAME, new MixinClassTransformer());
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
