package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.api.PluginLoader;
import dev.m00nl1ght.clockwork.classloading.ModuleManager;
import dev.m00nl1ght.clockwork.resolver.DependencyResolver;
import dev.m00nl1ght.clockwork.util.PluginLoadingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ClockworkCore implements ComponentTarget<ClockworkCore> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final List<PluginLoader> pluginLocators = new ArrayList<>();
    private final Map<String, ComponentTargetType<?>> componentTargets = new HashMap<>();
    private final Map<String, ComponentType<?, ?>> loadedComponents = new HashMap<>();
    private final Map<String, PluginContainer<?>> loadedPlugins = new HashMap<>();
    private final ModuleManager moduleManager = new ModuleManager();

    protected ClockworkCore() {}
    private static final ClockworkCore INSTANCE = new ClockworkCore();
    public static ClockworkCore getInstance() {
        return INSTANCE;
    }

    public void loadPlugins() {
        final var depResolver = new DependencyResolver();
        pluginLocators.forEach(e -> e.load(depResolver::addDefinition));
        depResolver.resolveAndSort();

        final var fatalProblems = depResolver.getFatalProblems();
        if (!fatalProblems.isEmpty()) {
            LOGGER.error("The following fatal problems occured during dependency resolution:");
            for (var p : fatalProblems) LOGGER.error(p.format());
            throw PluginLoadingException.fatalLoadingProblems(fatalProblems);
        }

        final var skips = depResolver.getSkippedProblems();
        if (!skips.isEmpty()) {
            LOGGER.error("The following optional components have been skipped, because their dependencies are not present:");
            for (var p : skips) LOGGER.error(p.format());
        }

        moduleManager.init(depResolver.getPluginDefinitions());
        depResolver.getLoadingOrder().forEach(this::buildComponent);

    }

    private void buildComponent(ComponentDefinition def) {
        final var plugin = loadedPlugins.computeIfAbsent(def.getParent().getId(), p -> buildPlugin(def.getParent()));
        final var compClass = moduleManager.loadClassForPlugin(plugin, def.getComponentClass());
        final var target = componentTargets.get(def.getTargetId());
        if (target == null) throw PluginLoadingException.componentMissingTarget(def);
        final var component = new ComponentType<>(def, plugin, compClass, target);
        loadedComponents.put(component.getId(), component);
    }

    private PluginContainer<?> buildPlugin(PluginDefinition def) {
        final var mainModule = moduleManager.mainModuleFor(def);
        final var plugin = new PluginContainer<>(def, mainModule);
        loadedPlugins.put(plugin.getId(), plugin);
        for (var targetDef : def.getTargetDefinitions()) buildComponentTarget(targetDef, plugin);
        return plugin;
    }

    private void buildComponentTarget(ComponentTargetDefinition def, PluginContainer<?> parent) {
        final var targetClass = moduleManager.loadClassForPlugin(parent, def.getTargetClass());
        final var target = new ComponentTargetType<>(def, parent, targetClass);
        componentTargets.put(target.getId(), target);
    }

    public void registerLocator(PluginLoader locator) {
        pluginLocators.add(locator);
    }

    @Override
    public <C> Optional<C> getComponent(ComponentType<C, ClockworkCore> componentType) {
        return Optional.empty(); //TODO
    }

}
