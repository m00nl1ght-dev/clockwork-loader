package dev.m00nl1ght.clockwork.processor;

import dev.m00nl1ght.clockwork.core.ComponentTargetType;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.PluginContainer;

import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

public interface PluginProcessor {

    default void process(PluginContainer plugin) throws Throwable {}

    default <C, T> void process(ComponentType<C, T> component, Supplier<MethodHandles.Lookup> reflectiveAccess) throws Throwable {}

    default <T> void process(ComponentTargetType<T> target, Supplier<MethodHandles.Lookup> reflectiveAccess) throws Throwable {}

    String getName();

}
