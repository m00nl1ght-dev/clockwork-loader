package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.util.Arguments;

import java.lang.module.ModuleFinder;
import java.lang.module.ResolvedModule;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class ModuleLayerPluginFinder extends AbstractPluginFinder {

    public static final String NAME = "internal.pluginfinder.modulelayer";
    public static final PluginFinderType FACTORY = ModuleLayerPluginFinder::new;

    protected final ModuleLayer moduleLayer;
    protected final Predicate<ResolvedModule> filter;

    public static void registerTo(ClockworkLoader loader) {
        Arguments.notNull(loader, "loader");
        loader.registerFinderType(NAME, FACTORY);
    }

    public static void registerTo(CollectClockworkExtensionsEvent event) {
        Arguments.notNull(event, "event");
        event.registerLocatorFactory(NAME, FACTORY);
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

    protected ModuleLayerPluginFinder(ModuleLayer moduleLayer, Predicate<ResolvedModule> filter, PluginFinderConfig config, Set<PluginReader> readers) {
        super(config, readers);
        this.moduleLayer = moduleLayer;
        this.filter = filter;
    }

    protected ModuleLayerPluginFinder(PluginFinderConfig config, Set<PluginReader> readers) {
        this(ModuleLayer.boot(), ModuleLayerPluginFinder::systemModuleFilter, config, readers);
    }

    @Override
    protected void scan() {
        moduleLayer.configuration().modules().stream().filter(filter)
                .map(m -> tryReadFromModule(m.reference(), null))
                .filter(Optional::isPresent).map(Optional::get)
                .forEach(this::found);
    }

    private static boolean systemModuleFilter(ResolvedModule module) {
        final var location = module.reference().location();
        return location.isPresent() && !location.get().getScheme().equals("jrt");
    }

    @Override
    public ModuleFinder getModuleFinder() {
        return null;
    }

}
