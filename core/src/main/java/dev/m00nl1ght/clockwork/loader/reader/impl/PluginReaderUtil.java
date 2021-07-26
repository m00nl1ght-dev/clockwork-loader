package dev.m00nl1ght.clockwork.loader.reader.impl;

import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;

import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public class PluginReaderUtil {

    private PluginReaderUtil() {}

    public static Optional<PluginDescriptor> tryReadDirect(Collection<PluginReader> readers, Path path) {
        return readers.stream()
                .map(reader -> reader.read(path))
                .filter(Optional::isPresent)
                .map(Optional::get).findFirst();
    }

    public static Optional<PluginDescriptor> tryRead(Collection<PluginReader> readers, Path path) {
        return tryAsModuleRoot(path, p -> tryReadDirect(readers, p));
    }

    public static Optional<PluginReference> tryReadFromModule(Collection<PluginReader> readers, ModuleReference moduleReference) {
        if (moduleReference.location().isEmpty()) return Optional.empty();
        final var path = Path.of(moduleReference.location().get());
        return tryRead(readers, path).map(descriptor ->
                PluginReference.of(descriptor, ModuleFinder.of(path), moduleReference.descriptor().name()));
    }

    public static <T> Optional<T> tryAsModuleRoot(Path path, Function<Path, Optional<T>> function) {
        if (Files.isDirectory(path)) {
            return function.apply(path);
        } else if (Files.isRegularFile(path)) {
            try (final var fs = FileSystems.newFileSystem(path, (ClassLoader) null)) {
                return function.apply(fs.getPath(""));
            } catch (IOException | FileSystemNotFoundException e) {
                throw new RuntimeException("Failed to open as filesystem: " + path, e);
            }
        } else {
            return Optional.empty();
        }
    }

}
