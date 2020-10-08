package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.core.LoadingContext;
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
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractPluginFinder implements PluginFinder {

    private static final Logger LOGGER = LogManager.getLogger();

    protected final PluginFinderConfig config;

    private Map<String, PluginReference> cache;

    protected AbstractPluginFinder(PluginFinderConfig config) {
        this.config = Arguments.notNull(config, "config");
    }

    @Override
    public Collection<PluginReference> findAll(LoadingContext context) {
        scanIfNeeded(context);
        return Set.copyOf(cache.values());
    }

    @Override
    public Collection<PluginReference> find(LoadingContext context, DependencyDescriptor target) {
        scanIfNeeded(context);
        final var found = cache.get(target.getPlugin());
        if (found != null && target.acceptsVersion(found.getVersion())) {
            return Collections.singleton(found);
        } else {
            return Collections.emptySet();
        }
    }

    protected void scanIfNeeded(LoadingContext context) {
        if (cache == null) {
            cache = new HashMap<>();
            if (config.getReaders() != null) {
                scan(context, config.getReaders().stream().map(context::getReader).collect(Collectors.toUnmodifiableList()));
            } else {
                scan(context, context.getReaders());
            }
        }
    }

    protected void found(PluginReference def) {
        final var prev = cache.putIfAbsent(def.getId(), def);
        if (prev != null) throw PluginLoadingException.pluginDuplicate(def.getDescriptor(), prev.getDescriptor());
    }

    protected abstract void scan(LoadingContext context, Collection<PluginReader> readers);

    protected Optional<PluginDescriptor> tryReadDirect(Collection<PluginReader> readers, Path path) {
        return readers.stream()
                .map(reader -> reader.read(path))
                .filter(Optional::isPresent)
                .map(Optional::get).findFirst();
    }

    protected Optional<PluginDescriptor> tryRead(Collection<PluginReader> readers, Path path) {
        return tryAsModuleRoot(path, p -> tryReadDirect(readers, p));
    }

    protected Optional<PluginReference> tryReadFromModule(Collection<PluginReader> readers, ModuleReference moduleReference, ModuleFinder finder) {
        if (moduleReference.location().isEmpty()) return Optional.empty();
        try {
            final var path = Path.of(moduleReference.location().get());
            return tryRead(readers, path).map(descriptor -> PluginReference.of(descriptor, moduleReference));
        } catch (PluginLoadingException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.debug("Failed to find plugin from module [" + moduleReference.descriptor().name() + "]", e);
            return Optional.empty();
        }
    }

    protected <T> Optional<T> tryAsModuleRoot(Path path, Function<Path, Optional<T>> function) {
        if (Files.isDirectory(path)) {
            return function.apply(path);
        } else if (Files.isRegularFile(path)) {
            try (final var fs = FileSystems.newFileSystem(path, (ClassLoader) null)) {
                return function.apply(fs.getPath(""));
            } catch (IOException | FileSystemNotFoundException e) {
                LOGGER.error("Failed to open as filesystem: " + path, e);
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return config.getType() + "[" + config.getName() +  "]";
    }

}
