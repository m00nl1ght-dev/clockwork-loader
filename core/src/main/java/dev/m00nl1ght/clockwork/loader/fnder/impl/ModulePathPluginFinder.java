package dev.m00nl1ght.clockwork.loader.fnder.impl;

import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.loader.reader.impl.PluginReaderUtil;
import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;

import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static dev.m00nl1ght.clockwork.utils.config.ConfigValue.*;

public class ModulePathPluginFinder extends AbstractScanningPluginFinder {

    public static final String TYPE = "internal.pluginfinder.modulepath";
    public static final Spec SPEC = new Spec();

    protected final ModuleFinder moduleFinder;

    public static void registerTo(ClockworkLoader loader) {
        loader.getFeatureProviders().register(PluginFinder.class, TYPE, ModulePathPluginFinder::new);
    }

    public static ModifiableConfig newConfig(String name, Path modulePath, boolean wildcard) {
        return newConfig(name, modulePath, null, wildcard);
    }

    public static ModifiableConfig newConfig(String name, Path modulePath, List<String> readers, boolean wildcard) {
        return Config.newConfig(SPEC)
                .put(SPEC.FEATURE_TYPE, TYPE)
                .put(SPEC.FEATURE_NAME, Objects.requireNonNull(name))
                .put(SPEC.READERS, readers)
                .put(SPEC.WILDCARD, wildcard)
                .put(SPEC.MODULE_PATH, modulePath);
    }

    protected ModulePathPluginFinder(ClockworkLoader loader, Config config) {
        super(loader, config);
        this.moduleFinder = ModuleFinder.of(config.get(SPEC.MODULE_PATH));
    }

    @Override
    protected Set<PluginReference> scan(ClockworkLoader loader, Collection<PluginReader> readers) {
        return moduleFinder.findAll().stream()
                .map(m -> PluginReaderUtil.tryReadFromModule(readers, m))
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String toString() {
        return TYPE + "[" + name +  "]";
    }

    public static class Spec extends AbstractPluginFinder.Spec {

        public final Entry<Path> MODULE_PATH = entry("modulePath", T_PATH).required();

        protected Spec(String specName) {
            super(specName);
        }

        private Spec() {
            super(TYPE);
            initialize();
        }

    }

}
