package dev.m00nl1ght.clockwork.classloading;

import dev.m00nl1ght.clockwork.core.PluginContainer;
import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;

import java.lang.module.ModuleFinder;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModuleManager {

    private final ModuleLayer moduleLayer;
    private ClassLoader urlLoader;
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
            return parent.defineModulesWithOneLoader(config, ClassLoader.getSystemClassLoader());
        } catch (Exception e) {
            throw PluginLoadingException.inModuleFinder(e, null);
        }
    }

    private URL transformURI(URI uri) {
        try {
            return uri.toURL();
        } catch (Exception e) {
            throw new RuntimeException("wut?", e);
        }
    }

    public Module mainModuleFor(PluginDefinition def) {
        final var moduleName = def.getMainModule();
        final var layer = def.getModuleFinder() == null ? ModuleLayer.boot() : moduleLayer;
        final var found = layer.findModule(moduleName);
        if (found.isEmpty()) throw PluginLoadingException.pluginMainModuleNotFound(def);

        if (def.getModuleFinder() == null) {
            modules.put(moduleName, def.getId());
        } else {
            final var pId = modules.get(moduleName);
            if (!def.getId().equals(pId)) throw PluginLoadingException.pluginMainModuleIllegal(def, pId);
        }

        return found.get();
    }

    public Class<?> loadClassForPlugin(String className, PluginContainer plugin) {
        try {
            final var cl = plugin == null ? Thread.currentThread().getContextClassLoader() : plugin.getMainModule().getClassLoader();
            final var clazz = Class.forName(className, false, cl);
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
