package dev.m00nl1ght.clockwork.loader.reader;

import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;

import java.nio.file.Path;
import java.util.Optional;

public interface PluginReader {

    Optional<PluginDescriptor> read(Path path);

}
