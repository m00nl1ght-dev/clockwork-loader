package dev.m00nl1ght.clockwork.processor;

import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.LoadedPlugin;
import dev.m00nl1ght.clockwork.core.TargetType;

import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

@SuppressWarnings("RedundantThrows")
public interface PluginProcessor {

    default void process(LoadedPlugin plugin) throws Throwable {}

    default void process(ComponentType<?, ?> component, Supplier<MethodHandles.Lookup> reflectiveAccess) throws Throwable {}

    default void process(TargetType<?> target, Supplier<MethodHandles.Lookup> reflectiveAccess) throws Throwable {}

    String getName();

}
