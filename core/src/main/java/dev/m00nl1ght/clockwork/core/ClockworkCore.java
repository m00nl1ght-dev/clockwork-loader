package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.classloading.ModuleManager;
import dev.m00nl1ght.clockwork.locator.PluginLocator;
import dev.m00nl1ght.clockwork.processor.PluginProcessorManager;
import dev.m00nl1ght.clockwork.resolver.DependencyResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * The most important class of the plugin loading framework.
 * It manages the loading process and represents all component
 * and target types of the loaded plugins.
 *
 * From application code, call {@link ClockworkCore#load} with a set of
 * {@link PluginLocator}s to get an instance of ClockworkCore.
 */
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
            @SuppressWarnings("unchecked") final var targetCasted = (Class<? extends ComponentTarget>) targetClass;
            final var target = TargetType.create(def, plugin, targetCasted, i);
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

    /**
     * Finds available plugins using the given {@link PluginLocator}s and resolves all dependencies.
     *
     * @param locators the collection of {@link PluginLocator}s that will be used to find plugins
     * @return a new {@link ClockworkCore} that can be used to load the plugins that have been located
     * @throws PluginLoadingException if there were any fatal dependency resolution problems
     */
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
            LOGGER.info("The following optional components have been skipped, because their dependencies are not present:");
            for (var p : skips) LOGGER.info(p.format());
        }

        return new ClockworkCore(depResolver);
    }

    /**
     * Initialises this ClockworkCore with a default core container.
     * The default core container is created for target id {@code clockwork:core}
     *
     * @see ClockworkCore#init(ComponentContainer)
     * @throws PluginLoadingException if no definition for the default target is present
     */
    public void init() {
        final var coreTarget = getTargetType(ClockworkCore.class);
        if (coreTarget.isEmpty()) throw PluginLoadingException.coreTargetMissing(CORE_TARGET_ID);
        this.init(new ComponentContainer<>(coreTarget.get(), this));
    }

    /**
     * Initialises this ClockworkCore with the given core container.
     * The core container is a special {@link ComponentContainer} that is attached to the ClockworkCore itself.
     * It will store all plugin components which exist in a static context
     * and are not attached to individual objects within the application.
     * For example, this includes the main component of each plugin.
     *
     * @param coreContainer the core container for this ClockworkCore
     */
    public synchronized void init(ComponentContainer<ClockworkCore> coreContainer) {
        if (this.state != State.LOCATED) throw new IllegalStateException();
        this.coreContainer = coreContainer;
        this.state = State.INITIALISED;
    }

    /**
     * Returns an unmodifiable collection of all registered {@link TargetType}s.
     */
    public Collection<TargetType<?>> getRegisteredTargetTypes() {
        return Collections.unmodifiableCollection(loadedTargets.values());
    }

    /**
     * Returns an unmodifiable collection of all registered {@link ComponentType}s.
     */
    public Collection<ComponentType<?, ?>> getRegisteredComponentTypes() {
        return Collections.unmodifiableCollection(loadedComponents.values());
    }

    /**
     * Returns the {@link TargetType} for the given target class, wrapped in an {@link Optional}.
     * If no such target is registered to this ClockworkCore, this method will return an empty optional.
     *
     * @param targetClass the class corresponding to the desired TargetType
     */
    @SuppressWarnings("unchecked")
    public <T extends ComponentTarget> Optional<TargetType<T>> getTargetType(Class<T> targetClass) {
        final var type = classToTargetMap.get(targetClass);
        if (type == null) return Optional.empty();
        return Optional.of((TargetType<T>) type);
    }

    /**
     * Returns the {@link TargetType} with the given id, wrapped in an {@link Optional}.
     * If no such target is registered to this ClockworkCore, this method will return an empty optional.
     *
     * @param targetId the id of the desired TargetType
     */
    public Optional<TargetType<?>> getTargetType(String targetId) {
        return Optional.ofNullable(loadedTargets.get(targetId));
    }

    /**
     * Returns the {@link ComponentType} for the given component and target classes, wrapped in an {@link Optional}.
     * If no such component is registered to this ClockworkCore, this method will return an empty optional.
     *
     * @param componentClass the class corresponding to the desired ComponentType
     * @param targetClass the class corresponding to the target of the desired ComponentType
     */
    @SuppressWarnings("unchecked")
    public <C, T extends ComponentTarget> Optional<ComponentType<C, T>> getComponentType(Class<C> componentClass, Class<T> targetClass) {
        final var type = classToComponentMap.get(componentClass);
        if (type == null) return Optional.empty();
        if (type.getTargetType().getTargetClass() != targetClass) return Optional.empty();
        return Optional.of((ComponentType<C, T>) type);
    }

    /**
     * Returns the {@link ComponentType} for the given component class, wrapped in an {@link Optional}.
     * If no such component is registered to this ClockworkCore, this method will return an empty optional.
     *
     * @param componentClass the class corresponding to the desired ComponentType
     */
    @SuppressWarnings("unchecked")
    public <C> Optional<ComponentType<C, ?>> getComponentType(Class<C> componentClass) {
        final var type = classToComponentMap.get(componentClass);
        if (type == null) return Optional.empty();
        return Optional.of((ComponentType<C, ?>) type);
    }

    /**
     * Returns the {@link ComponentType} with the given id, wrapped in an {@link Optional}.
     * If no such component is registered to this ClockworkCore, this method will return an empty optional.
     *
     * @param componentId the id of the desired ComponentType
     */
    public Optional<ComponentType<?, ?>> getComponentType(String componentId) {
        return Optional.ofNullable(loadedComponents.get(componentId));
    }

    /**
     * Returns the main {@link ComponentContainer} attached to this ClockworkCore.
     * It contains all plugin components which exist in a static context
     * and are not attached to individual objects within the application.
     * For example, this includes the main component of each plugin.
     *
     * @see ClockworkCore#init()
     */
    @Override
    public ComponentContainer<ClockworkCore> getComponentContainer() {
        return coreContainer;
    }

    /**
     * Returns the internal state of this ClockworkCore.
     */
    public State getState() {
        return state;
    }

    public enum State {

        /**
         * Plugins are being located and dependency resolution is not completed yet.
         */
        CONSTRUCTED,

        /**
         * All plugins have been located and dependencies have been resolved.
         * Component and target types are now available, and the core components
         * can be initialised by calling {@link ClockworkCore#init()}.
         */
        LOCATED,

        /**
         * Plugin loading is complete and all core components have been initialised.
         */
        INITIALISED

    }

}
