package dev.m00nl1ght.clockwork.api;

import dev.m00nl1ght.clockwork.core.PluginDefinition;

import java.util.function.Consumer;

public interface PluginLocator {

    void load(Consumer<PluginDefinition> pluginConsumer);

    String getName();

}