package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.api.PluginLoader;
import dev.m00nl1ght.clockwork.classloading.ModuleManager;
import dev.m00nl1ght.clockwork.resolver.DependencyResolver;
import dev.m00nl1ght.clockwork.util.PluginLoadingException;

import java.util.*;

public class ClockworkCore implements ComponentTarget<ClockworkCore> {

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
            // TODO log problems
            throw PluginLoadingException.fatalLoadingProblems(fatalProblems);
        }

        final var skips = depResolver.getSkippedProblems();
        if (!skips.isEmpty()) {
            // TODO log skipped components
        }

        moduleManager.init(depResolver.getPluginDefinitions());
        depResolver.getLoadingOrder().forEach(this::buildComponent);

    }

    private void buildComponent(ComponentDefinition def) {
        final var compClass = moduleManager.loadClassForPlugin(def.getComponentClass(), def.getParent().getId());
        final var plugin = loadedPlugins.computeIfAbsent(def.getParent().getId(), p -> buildPlugin(def.getParent(), compClass.getModule()));
        final var target = componentTargets.get(def.getTargetId());
        if (target == null) throw PluginLoadingException.componentMissingTarget(def);
        final var component = new ComponentType<>(def, plugin, compClass, target);
        loadedComponents.put(component.getId(), component);
    }

    private PluginContainer<?> buildPlugin(PluginDefinition def, Module module) {
        final var plugin = new PluginContainer<>(def, module);
        loadedPlugins.put(plugin.getId(), plugin);
        for (var targetDef : def.getTargetDefinitions()) buildComponentTarget(targetDef, plugin);
        return plugin;
    }

    private void buildComponentTarget(ComponentTargetDefinition def, PluginContainer<?> parent) {
        final var targetClass = moduleManager.loadClassForPlugin(def.getTargetClass(), parent.getId(), ComponentTarget.class);
        final var target = new ComponentTargetType<>(def, parent, targetClass);
        componentTargets.put(target.getId(), target);
    }

    public void registerLocator(PluginLoader locator) { // TODO move to builder?
        pluginLocators.add(locator);
    }

    public void registerComponentTarget(ComponentTargetType<?> targetType) { // TODO move to builder?
        componentTargets.put(targetType.getId(), targetType);
    }

    @Override
    public <C> Optional<C> getComponent(ComponentType<C, ClockworkCore> componentType) {
        return Optional.empty(); //TODO
    }

}
