package dev.m00nl1ght.clockwork.core;

public interface PluginProcessor {

    default void onLoadingStart(ClockworkCore core, ClockworkCore parentCore) {}

    void process(PluginProcessorContext context);

    default void onLoadingComplete(ClockworkCore core) {}

}
