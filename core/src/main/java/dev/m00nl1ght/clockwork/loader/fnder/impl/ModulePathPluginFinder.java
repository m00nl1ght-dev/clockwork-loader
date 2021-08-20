package dev.m00nl1ght.clockwork.loader.fnder.impl;

import dev.m00nl1ght.clockwork.config.Config;
import dev.m00nl1ght.clockwork.config.ImmutableConfig;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.loader.reader.impl.PluginReaderUtil;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ModulePathPluginFinder extends AbstractPluginFinder {

    public static final String TYPE = "internal.pluginfinder.modulepath";

    protected final ModuleFinder moduleFinder;

    public static void registerTo(ClockworkLoader loader) {
        loader.getFeatureProviders().register(PluginFinder.class, TYPE, ModulePathPluginFinder::new);
    }

    public static Config newConfig(String name, File modulePath, boolean wildcard) {
        return newConfig(name, modulePath, null, wildcard);
    }

    public static Config newConfig(String name, File modulePath, List<String> readers, boolean wildcard) {
        return ImmutableConfig.builder()
                .putString("type", TYPE)
                .putString("name", name)
                .putStrings("readers", readers)
                .putString("wildcard", wildcard)
                .putString("modulePath", modulePath.getPath())
                .build();
    }

    protected ModulePathPluginFinder(ClockworkLoader loader, Config config) {
        super(loader, config);
        this.moduleFinder = ModuleFinder.of(Path.of(config.get("modulePath")));
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
