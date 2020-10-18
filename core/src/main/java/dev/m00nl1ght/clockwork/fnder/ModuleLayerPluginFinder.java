package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.core.LoadingContext;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.fnder.PluginFinderConfig.Builder;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.reader.PluginReaderUtil;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.Registry;

import java.lang.module.ResolvedModule;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ModuleLayerPluginFinder extends AbstractPluginFinder {

    public static final String NAME = "internal.pluginfinder.modulelayer";
    public static final PluginFinderType FACTORY = ModuleLayerPluginFinder::new;

    protected final ModuleLayer moduleLayer;
    protected final Predicate<ResolvedModule> filter;

    public static void registerTo(Registry<PluginFinderType> registry) {
        Arguments.notNull(registry, "registry");
        registry.register(NAME, FACTORY);
    }

    public static Builder configBuilder(String name) {
        return PluginFinderConfig.builder(name, NAME);
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
    protected Set<PluginReference> scan(LoadingContext context, Collection<PluginReader> readers) {
        return moduleLayer.configuration().modules().stream().filter(filter)
                .map(m -> PluginReaderUtil.tryReadFromModule(readers, m.reference()))
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static boolean systemModuleFilter(ResolvedModule module) {
        final var location = module.reference().location();
        return location.isPresent() && !location.get().getScheme().equals("jrt");
    }

}
