package dev.m00nl1ght.clockwork.loader.processor;

import org.jetbrains.annotations.NotNull;

public interface PluginProcessor {

    default void processEarly(@NotNull PluginProcessorContext context) {}

    default void processLate(@NotNull PluginProcessorContext context) {}

}
