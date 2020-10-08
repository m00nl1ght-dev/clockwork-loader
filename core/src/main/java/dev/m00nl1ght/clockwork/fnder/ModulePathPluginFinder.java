package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.core.LoadingContext;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.Registry;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ModulePathPluginFinder extends AbstractPluginFinder {

    public static final String NAME = "internal.pluginfinder.modulepath";
    public static final PluginFinderType FACTORY = ModulePathPluginFinder::new;

    protected final ModuleFinder moduleFinder;

    public static void registerTo(Registry<PluginFinderType> registry) {
        Arguments.notNull(registry, "registry");
        registry.register(NAME, FACTORY);
    }

    public static PluginFinderConfig newConfig(String name, File modulePath) {
        return newConfig(name, modulePath, null);
    }

    public static PluginFinderConfig newConfig(String name, File modulePath, Set<String> readers) {
        return newConfig(name, modulePath, readers, false);
    }

    public static PluginFinderConfig newConfig(String name, File modulePath, Set<String> readers, boolean wildcard) {
        return new PluginFinderConfig(name, NAME, Map.of("modulePath", modulePath.getPath()), readers, wildcard);
    }

    protected ModulePathPluginFinder(PluginFinderConfig config) {
        super(config);
        this.moduleFinder = ModuleFinder.of(Path.of(config.get("modulePath")));
    }

    @Override
    protected void scan(LoadingContext context, Collection<PluginReader> readers) {
        moduleFinder.findAll().stream()
                .map(m -> tryReadFromModule(readers, m, moduleFinder))
                .filter(Optional::isPresent).map(Optional::get)
                .forEach(this::found);
    }

    @Override
    public ModuleFinder getModuleFinder(LoadingContext context) {
        return moduleFinder;
    }

}
