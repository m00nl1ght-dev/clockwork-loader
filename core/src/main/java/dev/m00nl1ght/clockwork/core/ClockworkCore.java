package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.classloading.ModuleManager;
import dev.m00nl1ght.clockwork.event.EventTypeRegistry;
import dev.m00nl1ght.clockwork.locator.PluginLocator;
import dev.m00nl1ght.clockwork.processor.PluginProcessorManager;
import dev.m00nl1ght.clockwork.resolver.DependencyResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClockworkCore implements ComponentTarget<ClockworkCore> {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final String CORE_PLUGIN_ID = "clockwork";
    public static final String CORE_TARGET_ID = CORE_PLUGIN_ID + ":core";

    private final Map<String, ComponentTargetType<?>> componentTargets = new HashMap<>();
    private final Map<Class<?>, ComponentTargetType<?>> classToTargetMap = new HashMap<>();
    private final Map<String, ComponentType<?, ?>> loadedComponents = new HashMap<>();
    private final Map<Class<?>, ComponentType<?, ?>> classToComponentMap = new HashMap<>();
    private final Map<String, PluginContainer> loadedPlugins = new HashMap<>();
    private final PluginProcessorManager processors = new PluginProcessorManager(MethodHandles.lookup());
    private final EventTypeRegistry eventTypeRegistry = new EventTypeRegistry();
    private final ComponentContainer<ClockworkCore> coreContainer;
    private final ModuleManager moduleManager;

    private ClockworkCore(DependencyResolver depResolver) {
        moduleManager = new ModuleManager(depResolver.getPluginDefinitions(), ModuleLayer.boot());
        depResolver.getPluginDefinitions().forEach(this::buildPlugin);
        depResolver.getTargetDefinitions().forEach(this::buildComponentTarget);
        depResolver.getComponentDefinitions().forEach(this::buildComponent);
        componentTargets.values().forEach(ComponentTargetType::lockRegistry);
        moduleManager.loadEventTypeRegistry(eventTypeRegistry);
        final var coreTarget = getTargetType(ClockworkCore.class);
        if (coreTarget.isEmpty()) throw PluginLoadingException.generic("core target is missing");
        coreContainer = new ComponentContainer<>(coreTarget.get(), this);
        coreContainer.initComponents();
    }

    public static ClockworkCore load(Collection<PluginLocator> locators) {
        final var depResolver = new DependencyResolver();
        locators.forEach(e -> e.findAll().forEach(p -> depResolver.addDefinition(p, e)));
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

        return new ClockworkCore(depResolver);
    }

    private void buildPlugin(PluginDefinition def) {
        final var mainModule = moduleManager.mainModuleFor(def);
        final var plugin = new PluginContainer(def, mainModule, this);
        moduleManager.bindModule(plugin, mainModule.getName());
        loadedPlugins.put(plugin.getId(), plugin);
        processors.apply(plugin, def.getProcessors());
    }

    private void buildComponent(ComponentDefinition def) {
        final var plugin = loadedPlugins.get(def.getParent().getId());
        if (plugin == null) throw new IllegalStateException("plugin vanished somehow");
        final var compClass = moduleManager.loadClassForPlugin(def.getComponentClass(), plugin);
        final var target = componentTargets.get(def.getTargetId());
        if (target == null) throw PluginLoadingException.componentMissingTarget(def);
        final var component = target.register(def, plugin, compClass);
        final var existingByName = loadedComponents.putIfAbsent(component.getId(), component);
        if (existingByName != null) throw PluginLoadingException.componentIdDuplicate(def, existingByName.getId());
        final var existingByClass = classToComponentMap.putIfAbsent(compClass, component);
        if (existingByClass != null) throw PluginLoadingException.componentClassDuplicate(def, existingByClass.getId());
        processors.apply(component, def.getProcessors());
    }

    private void buildComponentTarget(ComponentTargetDefinition def) {
        final var plugin = loadedPlugins.get(def.getPlugin().getId());
        if (plugin == null) throw new IllegalStateException("plugin vanished somehow");
        final var targetClass = moduleManager.loadClassForPlugin(def.getTargetClass(), plugin);
        final var target = new ComponentTargetType<>(def, plugin, targetClass);
        final var existingByName = componentTargets.putIfAbsent(target.getId(), target);
        if (existingByName != null) throw PluginLoadingException.targetIdDuplicate(def, existingByName.getId());
        final var existingByClass = classToTargetMap.putIfAbsent(targetClass, target);
        if (existingByClass != null) throw PluginLoadingException.targetClassDuplicate(def, existingByClass.getId());
        processors.apply(target, def.getProcessors());
    }

    @Override
    public <C> C getComponent(ComponentType<C, ? extends ClockworkCore> componentType) {
        return coreContainer == null ? null : coreContainer.getComponent(componentType);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<ComponentTargetType<T>> getTargetType(Class<T> targetClass) {
        final var type = classToTargetMap.get(targetClass);
        if (type == null) return Optional.empty();
        return Optional.of((ComponentTargetType<T>) type);
    }

    public Optional<ComponentTargetType<?>> getTargetType(String targetId) {
        return Optional.ofNullable(componentTargets.get(targetId));
    }

    @SuppressWarnings("unchecked")
    public <C, T> Optional<ComponentType<C, T>> getComponentType(Class<C> componentClass, Class<T> targetClass) {
        final var type = classToComponentMap.get(componentClass);
        if (type == null) return Optional.empty();
        if (type.getTargetType().getTargetClass() != targetClass) return Optional.empty();
        return Optional.of((ComponentType<C, T>) type);
    }

    @SuppressWarnings("unchecked")
    public <C> Optional<ComponentType<C, ?>> getComponentType(Class<C> componentClass) {
        final var type = classToComponentMap.get(componentClass);
        if (type == null) return Optional.empty();
        return Optional.of((ComponentType<C, ?>) type);
    }

    public Optional<ComponentType<?, ?>> getComponentType(String componentId) {
        return Optional.ofNullable(loadedComponents.get(componentId));
    }

    protected EventTypeRegistry getEventTypeRegistry() {
        return eventTypeRegistry;
    }

}
