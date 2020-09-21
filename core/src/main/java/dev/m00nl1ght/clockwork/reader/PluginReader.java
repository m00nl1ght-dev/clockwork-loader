package dev.m00nl1ght.clockwork.reader;

import dev.m00nl1ght.clockwork.descriptor.PluginReference;

import java.nio.file.Path;

public interface PluginReader {

    PluginReference.Builder read(Path path);

}
