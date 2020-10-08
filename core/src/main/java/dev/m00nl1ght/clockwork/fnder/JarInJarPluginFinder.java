package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.util.Arguments;

import java.lang.module.ModuleFinder;
import java.util.Map;
import java.util.Set;

public class JarInJarPluginFinder extends AbstractPluginFinder {

    public static final String NAME = "internal.pluginfinder.jarinjar";
    public static final PluginFinderType FACTORY = JarInJarPluginFinder::new;

    protected ModuleFinder moduleFinder;

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

    protected JarInJarPluginFinder(PluginFinderConfig config, Set<PluginReader> readers) {
        super(config, readers);
    }

    @Override
    protected void scan() {
        // TODO
    }

    @Override
    public ModuleFinder getModuleFinder() {
        scanIfNeeded();
        return moduleFinder;
    }

}
