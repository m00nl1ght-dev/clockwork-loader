package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.util.Arguments;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ModulePathPluginFinder extends AbstractPluginFinder {

    public static final String NAME = "internal.pluginfinder.modulepath";
    public static final PluginFinderType FACTORY = ModulePathPluginFinder::new;

    protected final ModuleFinder moduleFinder;

    public static void registerTo(ClockworkLoader loader) {
        Arguments.notNull(loader, "loader");
        loader.registerFinderType(NAME, FACTORY);
    }

    public static void registerTo(CollectClockworkExtensionsEvent event) {
        Arguments.notNull(event, "event");
        event.registerLocatorFactory(NAME, FACTORY);
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

    protected ModulePathPluginFinder(PluginFinderConfig config, Set<PluginReader> readers) {
        super(config, readers);
        this.moduleFinder = ModuleFinder.of(Path.of(config.get("modulePath")));
    }

    @Override
    protected void scan() {
        moduleFinder.findAll().stream()
                .map(m -> tryReadFromModule(m, moduleFinder))
                .filter(Optional::isPresent).map(Optional::get)
                .forEach(this::found);
    }

    @Override
    public ModuleFinder getModuleFinder() {
        return moduleFinder;
    }

}
