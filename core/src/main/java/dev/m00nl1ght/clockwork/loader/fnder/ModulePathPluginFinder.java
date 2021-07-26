package dev.m00nl1ght.clockwork.loader.fnder;

import dev.m00nl1ght.clockwork.loader.LoadingContext;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinderConfig.Builder;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.loader.reader.PluginReaderUtil;
import dev.m00nl1ght.clockwork.config.ImmutableConfig;
import dev.m00nl1ght.clockwork.util.Registry;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ModulePathPluginFinder extends AbstractPluginFinder {

    public static final String NAME = "internal.pluginfinder.modulepath";
    public static final PluginFinderType FACTORY = ModulePathPluginFinder::new;

    protected final ModuleFinder moduleFinder;

    public static void registerTo(Registry<PluginFinderType> registry) {
        Objects.requireNonNull(registry).register(NAME, FACTORY);
    }

    public static Builder configBuilder(String name, File modulePath) {
        return PluginFinderConfig.builder(name, NAME)
                .withParams(ImmutableConfig.builder()
                .putString("modulePath", modulePath.getPath())
                .build());
    }

    protected ModulePathPluginFinder(PluginFinderConfig config) {
        super(config);
        this.moduleFinder = ModuleFinder.of(Path.of(config.getParams().get("modulePath")));
    }

    @Override
    protected Set<PluginReference> scan(LoadingContext context, Collection<PluginReader> readers) {
        return moduleFinder.findAll().stream()
                .map(m -> PluginReaderUtil.tryReadFromModule(readers, m))
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toUnmodifiableSet());
    }

}
