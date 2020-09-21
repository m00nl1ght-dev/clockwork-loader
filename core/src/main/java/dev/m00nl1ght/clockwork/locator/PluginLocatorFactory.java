package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.reader.PluginReader;

import java.util.Set;

public interface PluginLocatorFactory {

    PluginLocator build(LocatorConfig config, Set<PluginReader> readers);

}
