package dev.m00nl1ght.clockwork.loader.reader;

import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;
import dev.m00nl1ght.clockwork.loader.reader.impl.ManifestPluginReader;
import dev.m00nl1ght.clockwork.utils.config.Config;

import java.nio.file.Path;
import java.util.Optional;

public interface PluginReader {

    Config DEFAULT = ManifestPluginReader.newConfig("manifestReader");

    Optional<PluginDescriptor> read(Path path);

}
