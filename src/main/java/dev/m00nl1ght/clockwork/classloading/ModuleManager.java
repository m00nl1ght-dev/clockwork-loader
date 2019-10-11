package dev.m00nl1ght.clockwork.classloading;

import dev.m00nl1ght.clockwork.core.PluginContainer;
import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.util.PluginLoadingException;

import java.lang.module.ModuleFinder;
import java.util.*;

public class ModuleManager {

    private ModuleLayer moduleLayer;
    private final Map<String, String> modules = new HashMap<>();

    public void init(List<PluginDefinition> defs) {
        defs.forEach(d -> findModules(d.getModuleFinder(), d));
        var finder = ModuleFinder.compose(defs.stream().map(PluginDefinition::getModuleFinder).toArray(ModuleFinder[]::new));
        this.moduleLayer = resolveModules(ModuleLayer.boot(), finder);
    }

    private void findModules(ModuleFinder finder, PluginDefinition owner) {
        try {
            finder.findAll().forEach(m -> modules.put(m.descriptor().name(), owner.getId()));
        } catch (Exception e) {
            throw PluginLoadingException.inModuleFinder(e, owner);
        }
    }

    private ModuleLayer resolveModules(ModuleLayer parent, ModuleFinder finder) {
        try {
            final var config = parent.configuration().resolve(ModuleFinder.of(), finder, modules.keySet());
            return parent.defineModules(config, this::createClassLoaderFor);
        } catch (Exception e) {
            throw PluginLoadingException.inModuleFinder(e, null);
        }
    }

    private ClassLoader createClassLoaderFor(String moduleName) {
        final var pluginId = modules.get(moduleName);
        if (pluginId == null) throw PluginLoadingException.generic("Cannot create classloader for unknown module [" + moduleName + "]");
        return new PluginClassloader(pluginId);
    }

    public Module mainModuleFor(PluginDefinition def) {
        final var pId = modules.get(def.getMainModule());
        if (!def.getId().equals(pId)) throw PluginLoadingException.pluginMainModule(def, pId);
        final var found = moduleLayer.findModule(def.getMainModule());
        return found.get();
    }

    public Class<?> loadClassForPlugin(PluginContainer<?> plugin, String className) {
        try {
            final var clazz = plugin.getMainModule().getClassLoader().loadClass(className);
            final var md = clazz.getModule().getDescriptor().name();
            final var actPlugin = modules.get(md);
            if (plugin.getId().equals(actPlugin)) {
                return clazz;
            } else {
                throw PluginLoadingException.componentClassIllegal(className, plugin, actPlugin, md);
            }
        } catch (ClassNotFoundException e) {
            throw PluginLoadingException.componentClassNotFound(className, plugin);
        }
    }

}
