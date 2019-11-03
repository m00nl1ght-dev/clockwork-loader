package dev.m00nl1ght.clockwork.classloading;

import dev.m00nl1ght.clockwork.core.PluginContainer;
import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;

import java.lang.module.ModuleFinder;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModuleManager {

    private final ModuleLayer.Controller layerController;
    private final Map<String, String> modules = new HashMap<>();
    private final Map<URL, PluginContainer> codeSourceToPlugin = new HashMap<>();
    private final Module localModule = ModuleManager.class.getModule();

    public ModuleManager(List<PluginDefinition> defs) {
        defs.forEach(d -> findModules(d.getModuleFinder(), d));
        final var comp = defs.stream().map(PluginDefinition::getModuleFinder).filter(Objects::nonNull);
        final var finder = ModuleFinder.compose(comp.toArray(ModuleFinder[]::new));
        this.layerController = buildLayer(ModuleLayer.boot(), finder);
    }

    private void findModules(ModuleFinder finder, PluginDefinition owner) {
        try {
            if (finder != null) finder.findAll().forEach(m -> modules.put(m.descriptor().name(), owner.getId()));
        } catch (Exception e) {
            throw PluginLoadingException.inModuleFinder(e, owner);
        }
    }

    private ModuleLayer.Controller buildLayer(ModuleLayer parent, ModuleFinder finder) {
        try {
            final var config = parent.configuration().resolve(ModuleFinder.of(), finder, modules.keySet());
            final var loader = new PluginClassloader(config.modules(), ClassLoader.getSystemClassLoader(), this);
            loader.initRemotePackageMap(config, List.of(parent));
            return ModuleLayer.defineModules(config, List.of(parent), m -> loader);
        } catch (Exception e) {
            throw PluginLoadingException.inModuleFinder(e, null);
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

    // TODO better way?
    public void bindModule(PluginContainer plugin, String moduleName) {
        final var module = layerController.layer().configuration().findModule(moduleName);
        if (module.isPresent() && module.get().reference().location().isPresent()) {
            try {
                codeSourceToPlugin.put(module.get().reference().location().get().toURL(), plugin);
            } catch (Exception e) {
                // ignored
            }
        }
    }

    public PluginContainer getPluginFor(URL codeSource) {
        return codeSourceToPlugin.get(codeSource);
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

}
