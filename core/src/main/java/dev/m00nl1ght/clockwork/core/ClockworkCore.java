package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.locator.PluginLocator;
import dev.m00nl1ght.clockwork.classloading.ModuleManager;
import dev.m00nl1ght.clockwork.locator.BootLayerLocator;
import dev.m00nl1ght.clockwork.resolver.DependencyResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ClockworkCore implements ComponentTarget<ClockworkCore> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final List<PluginLocator> pluginLocators = new ArrayList<>();
    private final Map<String, ComponentTargetType<?>> componentTargets = new HashMap<>();
    private final Map<Class<?>, ComponentTargetType<?>> classToTargetMap = new HashMap<>();
    private final Map<String, ComponentType<?, ?>> loadedComponents = new HashMap<>();
    private final Map<Class<?>, ComponentType<?, ?>> classToComponentMap = new HashMap<>();
    private final Map<String, PluginContainer> loadedPlugins = new HashMap<>();

    private ModuleManager moduleManager;
    private ComponentContainer<ClockworkCore> coreContainer;

    private static final ClockworkCore INSTANCE = new ClockworkCore();
    public static ClockworkCore getInstance() {
        return INSTANCE;
    }

    protected ClockworkCore() {
        registerLocator(new BootLayerLocator());
    }

    public void registerLocator(PluginLocator locator) {
        pluginLocators.add(locator);
    }

    public void loadPlugins() {
        final var depResolver = new DependencyResolver();
        pluginLocators.forEach(e -> e.load(p -> depResolver.addDefinition(p, e)));
        depResolver.resolveAndSort();

        final var fatalProblems = depResolver.getFatalProblems();
        if (!fatalProblems.isEmpty()) {
            LOGGER.error("The following fatal problems occurred during dependency resolution:");
            for (var p : fatalProblems) LOGGER.error(p.format());
            throw PluginLoadingException.fatalLoadingProblems(fatalProblems);
        }

        final var skips = depResolver.getSkippedProblems();
        if (!skips.isEmpty()) {
            LOGGER.error("The following optional components have been skipped, because their dependencies are not present:");
            for (var p : skips) LOGGER.error(p.format());
        }

        moduleManager = new ModuleManager(depResolver.getPluginDefinitions());
        depResolver.getLoadingOrder().forEach(this::buildComponent);
        componentTargets.values().forEach(ComponentTargetType::lockRegistry);
        final var coreTarget = getTargetType(ClockworkCore.class);
        if (coreTarget.isEmpty()) throw PluginLoadingException.generic("core target is missing");
        coreContainer = new ComponentContainer<>(coreTarget.get(), this);
        coreContainer.initComponents();
    }

    private PluginContainer buildPlugin(PluginDefinition def) {
        final var mainModule = moduleManager.mainModuleFor(def);
        final var plugin = new PluginContainer(def, mainModule);
        for (var targetDef : def.getTargetDefinitions()) buildComponentTarget(targetDef, plugin);
        return plugin;
    }

    private void buildComponent(ComponentDefinition def) {
        final var plugin = loadedPlugins.computeIfAbsent(def.getParent().getId(), p -> buildPlugin(def.getParent()));
        final var compClass = moduleManager.loadClassForPlugin(def.getComponentClass(), plugin);
        final var existing = classToComponentMap.get(compClass);
        if (existing != null) throw PluginLoadingException.componentClassDuplicate(def, existing.getId());
        final var target = componentTargets.get(def.getTargetId());
        if (target == null) throw PluginLoadingException.componentMissingTarget(def);
        final var component = target.register(def, plugin, compClass);
        loadedComponents.put(component.getId(), component);
        classToComponentMap.put(compClass, component);
    }

    private void buildComponentTarget(ComponentTargetDefinition def, PluginContainer parent) {
        final var targetClass = moduleManager.loadClassForPlugin(def.getTargetClass(), parent);
        final var target = new ComponentTargetType<>(def, parent, targetClass);
        final var existing = classToTargetMap.get(targetClass);
        if (existing != null) throw PluginLoadingException.targetClassDuplicate(def, existing.getId());
        componentTargets.put(target.getId(), target);
        classToTargetMap.put(targetClass, target);
    }

    @Override
    public <C> Optional<C> getComponent(ComponentType<C, ClockworkCore> componentType) {
        return coreContainer == null ? Optional.empty() : coreContainer.getComponent(componentType);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<ComponentTargetType<T>> getTargetType(Class<T> targetClass) {
        final var type = classToTargetMap.get(targetClass);
        if (type == null) return Optional.empty();
        return Optional.of((ComponentTargetType<T>) type);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<ComponentType<T, ?>> getComponentType(Class<T> componentClass) {
        final var type = classToComponentMap.get(componentClass);
        if (type == null) return Optional.empty();
        return Optional.of((ComponentType<T, ?>) type);
    }

}
