package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.descriptor.PluginReference;

import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A helper class that manages the internal {@link ModuleLayer} from which plugin code is loaded.
 * A ModuleManager is only used internally within the {@link ClockworkCore} and should not be exposed to plugin code.
 */
public class ModuleManager {

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
            final var modules = plugins.stream().map(PluginReference::getModuleName).collect(Collectors.toUnmodifiableList());
            final var finders = plugins.stream().map(PluginReference::getModuleFinder).collect(Collectors.toUnmodifiableList());
            final var pluginMF = ModuleFinder.compose(finders.toArray(ModuleFinder[]::new));
            final var libraryMF = ModuleFinder.of(loadingContext.getConfig().getLibModulePath().toArray(Path[]::new));
            final var combinedMF = ModuleFinder.compose(pluginMF, libraryMF);
            final var config = parent.configuration().resolveAndBind(ModuleFinder.of(), combinedMF, modules);
            layerController = ModuleLayer.defineModulesWithOneLoader(config, List.of(parent), null);
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

    public ModuleLayer getModuleLayer() {
        return layerController.layer();
    }

}
