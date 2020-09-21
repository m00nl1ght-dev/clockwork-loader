package dev.m00nl1ght.clockwork.reader;

import dev.m00nl1ght.clockwork.descriptor.PluginReference;

import java.nio.file.Path;

public class TomlConfigReader implements PluginReader {

    public static final String NAME = "TomlConfigReader";

    @Override
    public PluginReference.Builder read(Path path) {
        final var pluginInfo = PluginInfoFile.loadFromDir(path);
        if (pluginInfo == null) return null;
        return pluginInfo.populatePluginBuilder();
    }

}
