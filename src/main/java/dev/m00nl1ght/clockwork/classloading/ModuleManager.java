package dev.m00nl1ght.clockwork.classloading;

import dev.m00nl1ght.clockwork.core.PluginContainer;
import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.util.PluginLoadingException;

import java.lang.module.ModuleFinder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModuleManager {

    private final ModuleLayer moduleLayer;
    private final Map<String, String> modules = new HashMap<>();

    public ModuleManager(List<PluginDefinition> defs) {
        defs.forEach(d -> findModules(d.getModuleFinder(), d));
        final var comp = defs.stream().map(PluginDefinition::getModuleFinder).filter(Objects::nonNull);
        final var finder = ModuleFinder.compose(comp.toArray(ModuleFinder[]::new));
        this.moduleLayer = resolveModules(ModuleLayer.boot(), finder);
    }

    private void findModules(ModuleFinder finder, PluginDefinition owner) {
        try {
            if (finder != null) finder.findAll().forEach(m -> modules.put(m.descriptor().name(), owner.getId()));
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
        if (pluginId == null) throw PluginLoadingException.loaderForUnknownModule(moduleName);
        return new PluginClassloader(pluginId);
    }

    public Module mainModuleFor(PluginDefinition def) {
        final var found = moduleLayer.findModule(def.getMainModule());
        if (found.isEmpty()) throw PluginLoadingException.pluginMainModuleNotFound(def);

        final var name = found.get().getName();
        if (def.getModuleFinder() == null) {
            modules.put(name, def.getId());
        } else {
            final var pId = modules.get(name);
            if (!def.getId().equals(pId)) throw PluginLoadingException.pluginMainModuleIllegal(def, pId);
        }

        return found.get();
    }

    public Class<?> loadClassForPlugin(PluginContainer plugin, String className) {
        try {
            final var clazz = Class.forName(className, false, plugin.getMainModule().getClassLoader());
            final var md = clazz.getModule().getDescriptor().name();
            final var actPlugin = modules.get(md);
            if (!plugin.getId().equals(actPlugin))
                throw PluginLoadingException.componentClassIllegal(className, plugin, actPlugin, md);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw PluginLoadingException.componentClassNotFound(className, plugin);
        }
    }

}
