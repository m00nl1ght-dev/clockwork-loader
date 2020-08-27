package dev.m00nl1ght.clockwork.core;

public interface PluginProcessor {

    default void init(ClockworkCore core) {}

    void process(PluginProcessorContext context);

}
