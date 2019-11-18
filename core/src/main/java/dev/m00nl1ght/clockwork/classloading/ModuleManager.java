package dev.m00nl1ght.clockwork.classloading;

import dev.m00nl1ght.clockwork.core.PluginContainer;
import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import dev.m00nl1ght.clockwork.core.EventDispatcherFactory;
import dev.m00nl1ght.clockwork.core.EventDispatcherRegistry;

import java.lang.module.ModuleFinder;
import java.util.*;

public class ModuleManager {

    private final PluginClassloader classloader;
    private final ModuleLayer.Controller layerController;
    private final Map<String, String> modules = new HashMap<>();
    private final Module localModule = ModuleManager.class.getModule();

    public ModuleManager(List<PluginDefinition> defs, ModuleLayer parent) {
        try {
            defs.forEach(d -> findModules(d.getModuleFinder(), d));
            final var comp = defs.stream().map(PluginDefinition::getModuleFinder).filter(Objects::nonNull);
            final var finder = ModuleFinder.compose(comp.toArray(ModuleFinder[]::new));
            final var config = parent.configuration().resolveAndBind(ModuleFinder.of(), finder, modules.keySet());
            classloader = new PluginClassloader(config.modules(), ClassLoader.getSystemClassLoader(), this);
            classloader.initRemotePackageMap(config, List.of(parent));
            layerController = ModuleLayer.defineModules(config, List.of(parent), m -> classloader);
        } catch (Exception e) {
            throw PluginLoadingException.resolvingModules(e, null);
        }
    }

    private void findModules(ModuleFinder finder, PluginDefinition owner) {
        try {
            if (finder != null) finder.findAll().forEach(m -> modules.put(m.descriptor().name(), owner.getId()));
        } catch (Exception e) {
            throw PluginLoadingException.resolvingModules(e, owner);
        }
    }

    public Module mainModuleFor(PluginDefinition def) {
        final var moduleName = def.getMainModule();
        final var layer = def.getModuleFinder() == null ? ModuleLayer.boot() : layerController.layer();
        final var found = layer.findModule(moduleName);
        if (found.isEmpty()) throw PluginLoadingException.pluginMainModuleNotFound(def);

        if (def.getModuleFinder() == null) {
            modules.put(moduleName, def.getId());
        } else {
            final var pId = modules.get(moduleName);
            if (!def.getId().equals(pId)) throw PluginLoadingException.pluginMainModuleIllegal(def, pId);
            patchModule(found.get());
        }

        return found.get();
    }

    public void bindModule(PluginContainer plugin, String moduleName) {
        classloader.bindPlugin(plugin, moduleName);
    }

    private void patchModule(Module module) {
        for (var pn : module.getPackages()) {
            layerController.addOpens(module, pn, localModule);
        }
    }

    public Class<?> loadClassForPlugin(String className, PluginContainer plugin) {
        try {
            final var cl = plugin.getMainModule().getClassLoader();
            final var clazz = Class.forName(className, false, cl);
            final var md = clazz.getModule().getDescriptor();
            final var actPlugin = md == null ? null : modules.get(md.name());
            if (!plugin.getId().equals(actPlugin))
                throw PluginLoadingException.componentClassIllegal(className, plugin, actPlugin, md);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw PluginLoadingException.componentClassNotFound(className, plugin);
        }
    }

    public void loadEventTypeRegistry(EventDispatcherRegistry registry) {
        final var loader = ServiceLoader.load(layerController.layer(), EventDispatcherFactory.class);
        for (var factory : loader) registry.register(factory);
    }

}
