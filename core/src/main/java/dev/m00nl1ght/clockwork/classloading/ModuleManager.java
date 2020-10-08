package dev.m00nl1ght.clockwork.classloading;

import dev.m00nl1ght.clockwork.core.*;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;

import java.lang.module.ModuleFinder;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A helper class that manages the internal {@link ModuleLayer} from which plugin code is loaded.
 * A ModuleManager is only used internally within the {@link ClockworkCore} and should not be exposed to plugin code.
 */
public class ModuleManager {

    private final PluginClassloader classloader;
    private final ModuleLayer.Controller layerController;
    private final Module localModule = ModuleManager.class.getModule();

    /**
     * Constructs a new ModuleManager for the specific set of plugin definitions.
     * This constructor is called during plugin loading, after all definitions have been located,
     * but before any components or classes are loaded.
     *
     * @param parent the module layer that will be used as a parent for the plugin module layer (usually the boot layer)
     */
    public ModuleManager(LoadingContext loadingContext, Collection<PluginReference> plugins, ModuleLayer parent) {
        try {
            final var pluginModules = plugins.stream()
                    .map(p -> p.getMainModule().descriptor().name()).collect(Collectors.toUnmodifiableList());
            final var comp = loadingContext.getFinders().stream()
                    .map(finder -> finder.getModuleFinder(loadingContext)).filter(Objects::nonNull);
            final var finder = ModuleFinder.compose(comp.toArray(ModuleFinder[]::new));
            final var config = parent.configuration().resolveAndBind(ModuleFinder.of(), finder, pluginModules);
            classloader = new PluginClassloader(config.modules(), ClassLoader.getSystemClassLoader(), this);
            classloader.initRemotePackageMap(config, List.of(parent));
            plugins.forEach(classloader::bindPlugin);
            layerController = ModuleLayer.defineModules(config, List.of(parent), m -> classloader);
        } catch (Exception e) {
            throw PluginLoadingException.resolvingModules(e, null);
        }
    }

    /**
     * Patches the module to allow reflective access to its classes.
     * This is needed for {@link PluginProcessor}s that use annotation processing and method handles to work.
     */
    public void patchModule(Module module) {
        if (module.getLayer() == layerController.layer()) {
            for (var pn : module.getPackages()) {
                layerController.addOpens(module, pn, localModule);
            }
        }
    }

    /**
     * Loads the class with the given name from the internal module layer or any parent layers
     * and verifies that it was loaded from the main module of the specified plugin.
     *
     * @param className the qualified name of the class to be loaded
     * @param plugin    the plugin the class should be loaded from
     * @throws PluginLoadingException if the class is in a module other than
     *                                the main module of the plugin, or the class was not found
     */
    public Class<?> loadClassForPlugin(String className, LoadedPlugin plugin) {
        try {
            final var clazz = Class.forName(className, false, classloader);
            if (clazz.getModule() != plugin.getMainModule())
                throw PluginLoadingException.pluginClassIllegal(clazz, plugin);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw PluginLoadingException.pluginClassNotFound(className, plugin.getDescriptor());
        }
    }

    /**
     * Loads the class with the given name from the internal module layer or any parent layers.
     *
     * @param className the qualified name of the class to be loaded
     * @return the loaded class, or null if no such class was found
     */
    public Class<?> loadClassOrNull(String className) {
        try {
            return Class.forName(className, false, classloader);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public ModuleLayer getModuleLayer() {
        return layerController.layer();
    }

}
