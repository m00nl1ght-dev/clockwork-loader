package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.util.Arguments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public abstract class AbstractPluginFinder implements PluginFinder {

    private static final Logger LOGGER = LogManager.getLogger();

    protected final Set<PluginReader> readers;
    protected final PluginFinderConfig config;

    private Map<String, PluginReference> cache;

    protected AbstractPluginFinder(PluginFinderConfig config, Set<PluginReader> readers) {
        this.config = Arguments.notNull(config, "config");
        this.readers = Set.copyOf(Arguments.notNull(readers, "readers"));
    }

    @Override
    public Collection<PluginReference> findAll() {
        scanIfNeeded();
        return Set.copyOf(cache.values());
    }

    @Override
    public Collection<PluginReference> find(DependencyDescriptor target) {
        scanIfNeeded();
        final var found = cache.get(target.getPlugin());
        if (found != null && target.acceptsVersion(found.getVersion())) {
            return Collections.singleton(found);
        } else {
            return Collections.emptySet();
        }
    }

    protected void scanIfNeeded() {
        if (cache == null) {
            cache = new HashMap<>();
            scan();
        }
    }

    protected void found(PluginReference def) {
        final var prev = cache.putIfAbsent(def.getId(), def);
        if (prev != null) throw PluginLoadingException.pluginDuplicate(def.getDescriptor(), prev.getDescriptor());
    }

    protected abstract void scan();

    protected Optional<PluginDescriptor> tryReadDirect(Path path) {
        return readers.stream()
                .map(reader -> reader.read(path))
                .filter(Optional::isPresent)
                .map(Optional::get).findFirst();
    }

    protected Optional<PluginDescriptor> tryRead(Path path) {
        if (Files.isDirectory(path)) {
            return tryReadDirect(path);
        } else if (Files.isRegularFile(path)) {
            try (final var fs = FileSystems.newFileSystem(path, (ClassLoader) null)) {
                return tryReadDirect(fs.getPath(""));
            } catch (IOException | FileSystemNotFoundException e) {
                LOGGER.error("Failed to open as filesystem: " + path, e);
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    protected Optional<PluginReference> tryReadFromModule(ModuleReference moduleReference, ModuleFinder finder) {
        if (moduleReference.location().isEmpty()) return Optional.empty();
        final var moduleName = moduleReference.descriptor().name();
        try {
            final var path = Path.of(moduleReference.location().get());
            return tryRead(path).map(descriptor -> PluginReference.of(descriptor, this, moduleName));
        } catch (PluginLoadingException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.debug("Failed to find plugin from module [" + moduleName + "]", e);
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return config.getType() + "[" + config.getName() +  "]";
    }

}
