package dev.m00nl1ght.clockwork.reader;

import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;

import java.nio.file.Path;

public interface PluginReader {

    PluginDescriptor read(Path path);

}
