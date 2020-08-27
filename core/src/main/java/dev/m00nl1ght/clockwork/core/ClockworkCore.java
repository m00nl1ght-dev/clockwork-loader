package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.classloading.ModuleManager;
import dev.m00nl1ght.clockwork.util.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * This class represents a collection of {@link LoadedPlugin}s,
 * and the {@link ComponentType}s and {@link TargetType}s they provide.
 */
public class ClockworkCore implements ComponentTarget {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String CORE_PLUGIN_ID = "clockwork";
    public static final String CORE_TARGET_ID = CORE_PLUGIN_ID + ":core";

    private final Map<String, LoadedPlugin> loadedPlugins = new HashMap<>();
    private final Map<String, TargetType<?>> loadedTargets = new HashMap<>();
    private final Map<Class<?>, TargetType<?>> classToTargetMap = new HashMap<>();
    private final Map<String, ComponentType<?, ?>> loadedComponents = new HashMap<>();
    private final Map<Class<?>, ComponentType<?, ?>> classToComponentMap = new HashMap<>();

    private final ModuleManager moduleManager;

    private volatile State state = State.POPULATING;
    private ComponentContainer<ClockworkCore> coreContainer;

    ClockworkCore(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
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
    public void init(ComponentContainer<ClockworkCore> coreContainer) {
        state.require(State.POPULATED);
        this.coreContainer = coreContainer;
        this.coreContainer.initComponents();
        this.state = State.INITIALISED;
    }

    /**
     * Returns an unmodifiable collection of all {@link LoadedPlugin}s.
     */
    public Collection<LoadedPlugin> getLoadedPlugins() {
        return Collections.unmodifiableCollection(loadedPlugins.values());
    }

    public Optional<LoadedPlugin> getLoadedPlugin(String pluginId) {
        return Optional.ofNullable(loadedPlugins.get(pluginId));
    }

    /**
     * Returns an unmodifiable collection of all loaded {@link TargetType}s.
     */
    public Collection<TargetType<?>> getLoadedTargetTypes() {
        return Collections.unmodifiableCollection(loadedTargets.values());
    }

    /**
     * Returns an unmodifiable collection of all loaded {@link ComponentType}s.
     */
    public Collection<ComponentType<?, ?>> getLoadedComponentTypes() {
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
        state.requireOrAfter(State.POPULATED);
        return coreContainer;
    }

    public ModuleLayer getModuleLayer() {
        return moduleManager.getModuleLayer();
    }

    /**
     * Returns the internal state of this ClockworkCore.
     */
    public State getState() {
        return state;
    }

    public enum State {

        /**
         *
         */
        POPULATING,

        PROCESSING,

        /**
         * All plugins have been located and dependencies have been resolved.
         * Component and target types are now available, and the core components
         * can be initialised by calling {@link ClockworkCore#init()}.
         */
        POPULATED,

        /**
         * Plugin loading is complete and all core components have been initialised.
         */
        INITIALISED;

        public void require(State state) {
            if (this != state)
                throw new IllegalStateException("Required state [" + state + "], but was [" + this + "]");
        }

        public void requireAfter(State state) {
            if (this.ordinal() <= state.ordinal())
                throw new IllegalStateException("Required state after [" + state + "], but was [" + this + "]");
        }

        public void requireBefore(State state) {
            if (this.ordinal() >= state.ordinal())
                throw new IllegalStateException("Required state before [" + state + "], but was [" + this + "]");
        }

        public void requireOrAfter(State state) {
            if (this.ordinal() < state.ordinal())
                throw new IllegalStateException("Required state [" + state + "] or after, but was [" + this + "]");
        }

        public void requireOrBefore(State state) {
            if (this.ordinal() > state.ordinal())
                throw new IllegalStateException("Required state [" + state + "] or before, but was [" + this + "]");
        }

    }

    // ### Internal ###

    void setState(State state) {
        Preconditions.notNull(state, "state");
        state.requireOrAfter(this.state);
        this.state = state;
    }

    Optional<TargetType<?>> getTargetTypeUncasted(Class<?> targetClass) {
        final var type = classToTargetMap.get(targetClass);
        if (type == null) return Optional.empty();
        return Optional.of(type);
    }

    Optional<ComponentType<?, ?>> getComponentTypeUncasted(Class<?> componentClass) {
        final var type = classToComponentMap.get(componentClass);
        if (type == null) return Optional.empty();
        return Optional.of(type);
    }

    void addLoadedPlugin(LoadedPlugin loadedPlugin) {
        state.require(State.POPULATING);
        final var existing = loadedPlugins.putIfAbsent(loadedPlugin.getId(), loadedPlugin);
        if (existing != null) throw PluginLoadingException.pluginDuplicate(loadedPlugin.getDescriptor(), existing.getDescriptor());
    }

    void addLoadedTargetType(TargetType<?> targetType) {
        state.require(State.POPULATING);
        final var existingByName = loadedTargets.putIfAbsent(targetType.getId(), targetType);
        if (existingByName != null) throw PluginLoadingException.targetIdDuplicate(targetType.getDescriptor(), existingByName.getId());
        final var existingByClass = classToTargetMap.putIfAbsent(targetType.getTargetClass(), targetType);
        if (existingByClass != null) throw PluginLoadingException.targetClassDuplicate(targetType.getDescriptor(), existingByClass.getId());
    }

    void addLoadedComponentType(ComponentType<?, ?> componentType) {
        state.require(State.POPULATING);
        final var existingByName = loadedComponents.putIfAbsent(componentType.getId(), componentType);
        if (existingByName != null) throw PluginLoadingException.componentIdDuplicate(componentType.getDescriptor(), existingByName.getId());
        final var existingByClass = classToComponentMap.putIfAbsent(componentType.getComponentClass(), componentType);
        if (existingByClass != null) throw PluginLoadingException.componentClassDuplicate(componentType.getDescriptor(), existingByClass.getId());
    }

}
