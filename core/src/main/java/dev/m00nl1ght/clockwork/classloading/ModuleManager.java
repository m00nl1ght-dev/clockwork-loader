package dev.m00nl1ght.clockwork.classloading;

import dev.m00nl1ght.clockwork.core.PluginContainer;
import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;

import java.lang.module.ModuleFinder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A helper class that manages the internal ModuleLayer from which plugin code is loaded.
 * A ModuleManager is only used internally within the ClockworkCore and should never be exposed to plugin code.
 */
public class ModuleManager {

    private final PluginClassloader classloader;
    private final ModuleLayer.Controller layerController;
    private final Map<String, String> modules = new HashMap<>();
    private final Module localModule = ModuleManager.class.getModule();

    /**
     * Constructs a new ModuleManager for the specific set of plugin definitions.
     * This constructor is called during plugin loading, after all definitions have been located,
     * but before any components or classes are loaded.
     *
     * @param defs   the list of plugin definitions this ModuleManager will find modules for
     * @param parent the module layer that will be used as a parent for the plugin module layer (usually the boot layer)
     */
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

    /**
     * Helper method that populates the internal module name to plugin id map.
     */
    private void findModules(ModuleFinder finder, PluginDefinition owner) {
        try {
            if (finder != null) finder.findAll().forEach(m -> modules.put(m.descriptor().name(), owner.getId()));
        } catch (Exception e) {
            throw PluginLoadingException.resolvingModules(e, owner);
        }
    }

    /**
     * Finds the main module for a specific PluginDefinition.
     * The ModuleLayer of the returned module can either be the internal plugin module layer of this ModuleManager, or the boot layer.
     * If the module was loaded from the boot layer, it will also be registered to the respective plugin id.
     * If needed, this method will also patch the returned module to allow reflective access to its classes.
     */
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

    /**
     * Registers a module to a loaded plugin.
     * This assigs the plugins permissions to the classes of the module.
     * This method should only be called before any classes of the module are loaded.
     * Every module can only be bound to one plugin.
     * If no module with the given name is present, or its location can not be determined,
     * then this method just has no effect, and will not throw any exception.
     */
    public void bindModule(PluginContainer plugin, String moduleName) {
        classloader.bindPlugin(plugin, moduleName);
    }

    /**
     * Patches the module to allow reflective access to its classes.
     * This is needed for PluginProcessors that use annotation processing and method handles to work.
     */
    private void patchModule(Module module) {
        for (var pn : module.getPackages()) {
            layerController.addOpens(module, pn, localModule);
        }
    }

    /**
     * Loads the class with the given name from the internal module layer or any parent layers
     * and verifies that it was loaded from the main module of the specified plugin.
     *
     * @param className the qualified name of the class to be loaded
     * @param plugin    the plugin the class should be loaded for
     * @throws PluginLoadingException if the class is in a module other than
     *                                the main module of the plugin or the class was not found
     */
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

}
