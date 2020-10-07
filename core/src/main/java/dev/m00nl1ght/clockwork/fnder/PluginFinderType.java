package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.reader.PluginReader;

import java.util.Set;

public interface PluginFinderType {

    PluginFinder build(PluginFinderConfig config, Set<PluginReader> readers);

}
