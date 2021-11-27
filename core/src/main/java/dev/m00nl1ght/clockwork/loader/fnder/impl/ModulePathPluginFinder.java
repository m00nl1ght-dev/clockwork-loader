package dev.m00nl1ght.clockwork.loader.fnder.impl;

import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.loader.reader.impl.PluginReaderUtil;
import dev.m00nl1ght.clockwork.utils.config.*;
import dev.m00nl1ght.clockwork.utils.config.ConfigValue.Type;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec.Entry;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ModulePathPluginFinder extends AbstractScanningPluginFinder {

    public static final String TYPE = "internal.pluginfinder.modulepath";

    public static final ConfigSpec CONFIG_SPEC = ConfigSpec.create(TYPE, AbstractPluginFinder.CONFIG_SPEC);
    public static final Entry<String> CONFIG_ENTRY_MODULEPATH = CONFIG_SPEC.put("modulePath", ConfigValue.STRING).required();
    public static final Type<Config> CONFIG_TYPE = CONFIG_SPEC.buildType();

    protected final ModuleFinder moduleFinder;

    public static void registerTo(ClockworkLoader loader) {
        loader.getFeatureProviders().register(PluginFinder.class, TYPE, ModulePathPluginFinder::new);
    }

    public static ModifiableConfig newConfig(String name, File modulePath, boolean wildcard) {
        return newConfig(name, modulePath, null, wildcard);
    }

    public static ModifiableConfig newConfig(String name, File modulePath, List<String> readers, boolean wildcard) {
        return Config.newConfig(CONFIG_SPEC)
                .put(ConfiguredFeatures.CONFIG_ENTRY_TYPE, TYPE)
                .put(ConfiguredFeatures.CONFIG_ENTRY_NAME, Objects.requireNonNull(name))
                .put(AbstractPluginFinder.CONFIG_ENTRY_READERS, readers)
                .put(AbstractPluginFinder.CONFIG_ENTRY_WILDCARD, wildcard)
                .put(CONFIG_ENTRY_MODULEPATH, modulePath.getPath());
    }

    protected ModulePathPluginFinder(ClockworkLoader loader, Config config) {
        super(loader, config);
        this.moduleFinder = ModuleFinder.of(Path.of(config.get(CONFIG_ENTRY_MODULEPATH)));
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

}
