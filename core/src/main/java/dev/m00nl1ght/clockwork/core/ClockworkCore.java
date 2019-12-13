package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.classloading.ModuleManager;
import dev.m00nl1ght.clockwork.locator.PluginLocator;
import dev.m00nl1ght.clockwork.processor.PluginProcessorManager;
import dev.m00nl1ght.clockwork.resolver.DependencyResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class ClockworkCore implements ComponentTarget {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final String CORE_PLUGIN_ID = "clockwork";
    public static final String CORE_TARGET_ID = CORE_PLUGIN_ID + ":core";

    private final Map<String, TargetType<?>> loadedTargets = new HashMap<>();
    private final Map<Class<?>, TargetType<?>> classToTargetMap = new HashMap<>();
    private final Map<String, ComponentType<?, ?>> loadedComponents = new HashMap<>();
    private final Map<Class<?>, ComponentType<?, ?>> classToComponentMap = new HashMap<>();
    private final Map<String, PluginContainer> loadedPlugins = new HashMap<>();
    private final PluginProcessorManager pluginProcessors = new PluginProcessorManager(MethodHandles.lookup());
    private final ModuleManager moduleManager;
    private volatile State state = State.CONSTRUCTED;
    private ComponentContainer<ClockworkCore> coreContainer;

    @SuppressWarnings("unchecked")
    private ClockworkCore(DependencyResolver depResolver) {
        moduleManager = new ModuleManager(depResolver.getPluginDefinitions(), ModuleLayer.boot());

        final var pluginDefs = depResolver.getPluginDefinitions();
        for (int i = 0; i < pluginDefs.size(); i++) {
            final var def = pluginDefs.get(i);
            final var mainModule = moduleManager.mainModuleFor(def);
            final var plugin = new PluginContainer(def, mainModule, this);
            moduleManager.bindModule(plugin, mainModule.getName());
            loadedPlugins.put(plugin.getId(), plugin);
            pluginProcessors.apply(plugin, def.getProcessors());
        }

        final var targetDefs = depResolver.getTargetDefinitions();
        for (int i = 0; i < targetDefs.size(); i++) {
            final var def = targetDefs.get(i);
            final var plugin = loadedPlugins.get(def.getPlugin().getId());
            if (plugin == null) throw new IllegalStateException("plugin vanished somehow");
            final var targetClass = moduleManager.loadClassForPlugin(def.getTargetClass(), plugin);
            if (!ComponentTarget.class.isAssignableFrom(targetClass)) throw PluginLoadingException.invalidTargetClass(def);
            final var target = TargetType.create(def, plugin, (Class<? extends ComponentTarget>) targetClass, i);
            final var existingByName = loadedTargets.putIfAbsent(target.getId(), target);
            if (existingByName != null) throw PluginLoadingException.targetIdDuplicate(def, existingByName.getId());
            final var existingByClass = classToTargetMap.putIfAbsent(targetClass, target);
            if (existingByClass != null) throw PluginLoadingException.targetClassDuplicate(def, existingByClass.getId());
            pluginProcessors.apply(target, def.getProcessors());
        }

        final var componentDefs = depResolver.getComponentDefinitions();
        for (int i = 0; i < componentDefs.size(); i++) {
            final var def = componentDefs.get(i);
            final var plugin = loadedPlugins.get(def.getParent().getId());
            if (plugin == null) throw new IllegalStateException("plugin vanished somehow");
            final var compClass = moduleManager.loadClassForPlugin(def.getComponentClass(), plugin);
            final var target = loadedTargets.get(def.getTargetId());
            if (target == null) throw PluginLoadingException.componentMissingTarget(def);
            final var component = target.getPrimer().register(def, plugin, compClass);
            final var existingByName = loadedComponents.putIfAbsent(component.getId(), component);
            if (existingByName != null) throw PluginLoadingException.componentIdDuplicate(def, existingByName.getId());
            final var existingByClass = classToComponentMap.putIfAbsent(compClass, component);
            if (existingByClass != null) throw PluginLoadingException.componentClassDuplicate(def, existingByClass.getId());
            pluginProcessors.apply(component, def.getProcessors());
        }

        for (var targetType : loadedTargets.values()) targetType.getPrimer().init();
        this.state = State.LOCATED;
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

    public void init() {
        final var coreTarget = getTargetType(ClockworkCore.class);
        if (coreTarget.isEmpty()) throw PluginLoadingException.coreTargetMissing(CORE_TARGET_ID);
        this.init(new ComponentContainer<>(coreTarget.get(), this));
    }

    public synchronized void init(ComponentContainer<ClockworkCore> coreContainer) {
        if (this.state != State.LOCATED) throw new IllegalStateException();
        this.coreContainer = coreContainer;
        this.state = State.INITIALISED;
    }

    public Collection<TargetType<?>> getRegisteredTargetTypes() {
        return Collections.unmodifiableCollection(loadedTargets.values());
    }

    public Collection<ComponentType<?, ?>> getRegisteredComponentTypes() {
        return Collections.unmodifiableCollection(loadedComponents.values());
    }

    @SuppressWarnings("unchecked")
    public <T extends ComponentTarget> Optional<TargetType<T>> getTargetType(Class<T> targetClass) {
        final var type = classToTargetMap.get(targetClass);
        if (type == null) return Optional.empty();
        return Optional.of((TargetType<T>) type);
    }

    public Optional<TargetType<?>> getTargetType(String targetId) {
        return Optional.ofNullable(loadedTargets.get(targetId));
    }

    @SuppressWarnings("unchecked")
    public <C, T extends ComponentTarget> Optional<ComponentType<C, T>> getComponentType(Class<C> componentClass, Class<T> targetClass) {
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

    @Override
    public ComponentContainer<ClockworkCore> getComponentContainer() {
        return coreContainer;
    }

    public State getState() {
        return state;
    }

    public enum State {
        CONSTRUCTED, LOCATED, INITIALISED
    }

}
