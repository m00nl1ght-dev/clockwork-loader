package dev.m00nl1ght.clockwork.loader.fnder.impl;

import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.loader.reader.impl.PluginReaderUtil;
import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;

import java.lang.module.ResolvedModule;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ModuleLayerPluginFinder extends AbstractScanningPluginFinder {

    public static final String TYPE = "internal.pluginfinder.modulelayer";
    public static final Spec SPEC = new Spec();

    public static void registerTo(ClockworkLoader loader) {
        loader.getFeatureProviders().register(PluginFinder.class, TYPE, ModuleLayerPluginFinder::new);
    }

    public static ModifiableConfig newConfig(String name, boolean wildcard) {
        return newConfig(name, null, wildcard);
    }

    public static ModifiableConfig newConfig(String name, List<String> readers, boolean wildcard) {
        return Config.newConfig(SPEC)
                .put(SPEC.FEATURE_TYPE, TYPE)
                .put(SPEC.FEATURE_NAME, Objects.requireNonNull(name))
                .put(SPEC.READERS, readers)
                .put(SPEC.WILDCARD, wildcard);
    }

    protected final ModuleLayer moduleLayer;
    protected final Predicate<ResolvedModule> filter;

    public ModuleLayerPluginFinder(ClockworkLoader loader,
                                   ModuleLayer moduleLayer,
                                   Predicate<ResolvedModule> filter,
                                   Config config) {
        super(loader, config);
        this.moduleLayer = moduleLayer;
        this.filter = filter;
    }

    protected ModuleLayerPluginFinder(ClockworkLoader loader, Config config) {
        this(loader, ModuleLayer.boot(), ModuleLayerPluginFinder::systemModuleFilter, config);
    }

    @Override
    protected Set<PluginReference> scan(ClockworkLoader loader, Collection<PluginReader> readers) {
        return moduleLayer.configuration().modules().stream().filter(filter)
                .map(m -> PluginReaderUtil.tryReadFromModule(readers, m.reference()))
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static boolean systemModuleFilter(ResolvedModule module) {
        final var location = module.reference().location();
        return location.isPresent() && !location.get().getScheme().equals("jrt");
    }

    @Override
    public String toString() {
        return TYPE + "[" + name +  "]";
    }

    public static class Spec extends AbstractPluginFinder.Spec {

        protected Spec(String specName) {
            super(specName);
        }

        private Spec() {
            super(TYPE);
            initialize();
        }

    }

}
