package dev.m00nl1ght.clockwork.processor;

import dev.m00nl1ght.clockwork.core.LoadedPlugin;

public interface PluginProcessor {

    void process(LoadedPlugin plugin, ReflectiveAccess reflectiveAccess);

}
