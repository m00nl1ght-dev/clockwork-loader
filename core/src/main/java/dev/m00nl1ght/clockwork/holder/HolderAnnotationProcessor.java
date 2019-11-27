package dev.m00nl1ght.clockwork.holder;

import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.processor.PluginProcessor;

import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

public class HolderAnnotationProcessor implements PluginProcessor {

    public static final String NAME = "core.holder.annotation";

    @Override
    public void process(ComponentType<?, ?> component, Supplier<MethodHandles.Lookup> reflectiveAccess) {
        // TODO
    }

    @Override
    public void process(TargetType<?> target, Supplier<MethodHandles.Lookup> reflectiveAccess) {
        // TODO
    }

    @Override
    public String getName() {
        return NAME;
    }

}
