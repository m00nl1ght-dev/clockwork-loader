package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.core.LoadingContext;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.Registry;

import java.lang.module.ModuleFinder;
import java.lang.module.ResolvedModule;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class ModuleLayerPluginFinder extends AbstractPluginFinder {

    public static final String NAME = "internal.pluginfinder.modulelayer";
    public static final PluginFinderType FACTORY = ModuleLayerPluginFinder::new;

    protected final ModuleLayer moduleLayer;
    protected final Predicate<ResolvedModule> filter;

    public static void registerTo(Registry<PluginFinderType> registry) {
        Arguments.notNull(registry, "registry");
        registry.register(NAME, FACTORY);
    }

    public static PluginFinderConfig newConfig(String name) {
        return newConfig(name, false);
    }

    public static PluginFinderConfig newConfig(String name, boolean wildcard) {
        return newConfig(name, null, wildcard);
    }

    public static PluginFinderConfig newConfig(String name, Set<String> readers, boolean wildcard) {
        return new PluginFinderConfig(name, NAME, Map.of(), readers, wildcard);
    }

    protected ModuleLayerPluginFinder(ModuleLayer moduleLayer, Predicate<ResolvedModule> filter, PluginFinderConfig config) {
        super(config);
        this.moduleLayer = moduleLayer;
        this.filter = filter;
    }

    protected ModuleLayerPluginFinder(PluginFinderConfig config) {
        this(ModuleLayer.boot(), ModuleLayerPluginFinder::systemModuleFilter, config);
    }

    @Override
    protected void scan(LoadingContext context, Collection<PluginReader> readers) {
        moduleLayer.configuration().modules().stream().filter(filter)
                .map(m -> tryReadFromModule(readers, m.reference(), null))
                .filter(Optional::isPresent).map(Optional::get)
                .forEach(this::found);
    }

    private static boolean systemModuleFilter(ResolvedModule module) {
        final var location = module.reference().location();
        return location.isPresent() && !location.get().getScheme().equals("jrt");
    }

    @Override
    public ModuleFinder getModuleFinder(LoadingContext context) {
        return null;
    }

}
