package dev.m00nl1ght.clockwork.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PluginProcessor {

    default void onLoadingStart(@NotNull ClockworkCore core, @Nullable ClockworkCore parentCore) {}

    void process(@NotNull PluginProcessorContext context);

    default void onLoadingComplete(@NotNull ClockworkCore core) {}

}
