package dev.m00nl1ght.clockwork.classloading;

import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.util.PluginLoadingException;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.util.*;
import java.util.stream.Collectors;

public class ModuleManager {

    private ModuleLayer moduleLayer;
    private final Map<String, String> modules = new HashMap<>();

    public void init(List<PluginDefinition> defs) {
        var found = new ArrayList<String>();
        defs.forEach(d -> found.addAll(findModules(d.getModuleFinder(), d)));
        var finder = ModuleFinder.compose(defs.stream().map(PluginDefinition::getModuleFinder).toArray(ModuleFinder[]::new));
        this.moduleLayer = resolveModules(ModuleLayer.boot(), finder, found);
    }

    private List<String> findModules(ModuleFinder finder, PluginDefinition owner) {
        try {
            return finder.findAll().stream().map(r -> r.descriptor().name()).collect(Collectors.toList());
        } catch (Exception e) {
            throw PluginLoadingException.inModuleFinder(e, owner);
        }
    }

    private ModuleLayer resolveModules(ModuleLayer parent, ModuleFinder finder, Collection<String> modules) {
        try {
            Configuration config = parent.configuration().resolve(ModuleFinder.of(), finder, modules);
            return parent.defineModules(config, this::createClassLoaderFor);
        } catch (Exception e) {
            throw PluginLoadingException.inModuleFinder(e, null);
        }
    }

    private ClassLoader createClassLoaderFor(String moduleName) {

    }

    public Class<?> loadClassForPlugin(String className, String pluginId) {

    }

    public <T> Class<T> loadClassForPlugin(String className, String pluginId, Class<T> type) {

    }

}
