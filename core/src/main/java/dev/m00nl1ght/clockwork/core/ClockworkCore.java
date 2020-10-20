package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Arguments;

import java.util.*;

/**
 * This class represents a collection of {@link LoadedPlugin}s,
 * and the {@link ComponentType}s and {@link TargetType}s they provide.
 */
public class ClockworkCore implements ComponentTarget {

    public static final String CORE_PLUGIN_ID = "clockwork";
    public static final String CORE_TARGET_ID = CORE_PLUGIN_ID + ":core";

    private final Map<String, LoadedPlugin> loadedPlugins = new LinkedHashMap<>();
    private final Map<String, RegisteredTargetType<?>> loadedTargets = new LinkedHashMap<>();
    private final Map<Class<?>, RegisteredTargetType<?>> classToTargetMap = new LinkedHashMap<>();
    private final Map<String, RegisteredComponentType<?, ?>> loadedComponents = new LinkedHashMap<>();
    private final Map<Class<?>, RegisteredComponentType<?, ?>> classToComponentMap = new LinkedHashMap<>();

    private final ModuleLayer moduleLayer;

    private volatile State state = State.POPULATING;
    private ComponentContainer<ClockworkCore> coreContainer;

    ClockworkCore(ModuleLayer moduleLayer) {
        this.moduleLayer = Arguments.notNull(moduleLayer, "moduleLayer");
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
    public Collection<RegisteredTargetType<?>> getLoadedTargetTypes() {
        return Collections.unmodifiableCollection(loadedTargets.values());
    }

    /**
     * Returns the {@link TargetType} for the core target of this clockwork core.
     *
     * The core target contains all plugin components which exist in a static context
     * and are not attached to individual objects within the application.
     * For example, this includes the main component of each plugin.
     */
    @Override
    public RegisteredTargetType<ClockworkCore> getTargetType() {
        return getTargetType(ClockworkCore.class).orElseThrow();
    }

    /**
     * Returns the {@link TargetType} for the given target class, wrapped in an {@link Optional}.
     * If no such target is registered to this ClockworkCore, this method will return an empty optional.
     *
     * @param targetClass the class corresponding to the desired TargetType
     */
    @SuppressWarnings("unchecked")
    public <T extends ComponentTarget> Optional<RegisteredTargetType<T>> getTargetType(Class<T> targetClass) {
        final var type = classToTargetMap.get(targetClass);
        if (type == null) return Optional.empty();
        return Optional.of((RegisteredTargetType<T>) type);
    }

    /**
     * Returns the {@link TargetType} with the given id, wrapped in an {@link Optional}.
     * If no such target is registered to this ClockworkCore, this method will return an empty optional.
     *
     * @param targetId the id of the desired TargetType
     */
    public Optional<RegisteredTargetType<?>> getTargetType(String targetId) {
        return Optional.ofNullable(loadedTargets.get(targetId));
    }

    /**
     * Returns an unmodifiable collection of all loaded {@link ComponentType}s.
     */
    public Collection<RegisteredComponentType<?, ?>> getLoadedComponentTypes() {
        return Collections.unmodifiableCollection(loadedComponents.values());
    }

    /**
     * Returns the {@link ComponentType} for the given component and target classes, wrapped in an {@link Optional}.
     * If no such component is registered to this ClockworkCore, this method will return an empty optional.
     *
     * @param componentClass the class corresponding to the desired ComponentType
     * @param targetClass the class corresponding to the target of the desired ComponentType
     */
    @SuppressWarnings("unchecked")
    public <C, T extends ComponentTarget> Optional<RegisteredComponentType<C, T>> getComponentType(Class<C> componentClass, Class<T> targetClass) {
        final var type = classToComponentMap.get(componentClass);
        if (type == null) return Optional.empty();
        if (type.getTargetType().getTargetClass() != targetClass) return Optional.empty();
        return Optional.of((RegisteredComponentType<C, T>) type);
    }

    /**
     * Returns the {@link ComponentType} for the given component class, wrapped in an {@link Optional}.
     * If no such component is registered to this ClockworkCore, this method will return an empty optional.
     *
     * @param componentClass the class corresponding to the desired ComponentType
     */
    @SuppressWarnings("unchecked")
    public <C> Optional<RegisteredComponentType<C, ?>> getComponentType(Class<C> componentClass) {
        final var type = classToComponentMap.get(componentClass);
        if (type == null) return Optional.empty();
        return Optional.of((RegisteredComponentType<C, ?>) type);
    }

    /**
     * Returns the {@link ComponentType} with the given id and target classes, wrapped in an {@link Optional}.
     * If no such component is registered to this ClockworkCore, this method will return an empty optional.
     *
     * @param componentId the id of the desired ComponentType
     * @param targetClass the class corresponding to the target of the desired ComponentType
     */
    @SuppressWarnings("unchecked")
    public <T extends ComponentTarget> Optional<RegisteredComponentType<?, T>> getComponentType(String componentId, Class<T> targetClass) {
        final var type = loadedComponents.get(componentId);
        if (type == null) return Optional.empty();
        if (type.getTargetType().getTargetClass() != targetClass) return Optional.empty();
        return Optional.of((RegisteredComponentType<?, T>) type);
    }

    /**
     * Returns the {@link ComponentType} with the given id, wrapped in an {@link Optional}.
     * If no such component is registered to this ClockworkCore, this method will return an empty optional.
     *
     * @param componentId the id of the desired ComponentType
     */
    public Optional<RegisteredComponentType<?, ?>> getComponentType(String componentId) {
        return Optional.ofNullable(loadedComponents.get(componentId));
    }

    @Override
    public Object getComponent(int internalID) {
        try {
            return coreContainer.getComponent(internalID);
        } catch (Exception e) {
            state.requireOrAfter(State.PROCESSED);
            throw e;
        }
    }

    public ModuleLayer getModuleLayer() {
        return moduleLayer;
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

        /**
         * All plugins have been located and dependencies have been resolved.
         * Component and target types are now available.
         */
        POPULATED,

        /**
         * The core components can now be initialised by calling {@link ClockworkLoader#init()}.
         */
        PROCESSED,

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
        Arguments.notNull(state, "state");
        state.requireOrAfter(this.state);
        this.state = state;
    }

    void setCoreContainer(ComponentContainer<ClockworkCore> container) {
        Arguments.notNull(container, "container");
        if (this.coreContainer != null) throw new IllegalStateException();
        this.coreContainer = container;
    }

    Optional<RegisteredTargetType<?>> getTargetTypeUncasted(Class<?> targetClass) {
        final var type = classToTargetMap.get(targetClass);
        if (type == null) return Optional.empty();
        return Optional.of(type);
    }

    Optional<RegisteredComponentType<?, ?>> getComponentTypeUncasted(Class<?> componentClass) {
        final var type = classToComponentMap.get(componentClass);
        if (type == null) return Optional.empty();
        return Optional.of(type);
    }

    void addLoadedPlugin(LoadedPlugin loadedPlugin) {
        state.require(State.POPULATING);
        final var existing = loadedPlugins.putIfAbsent(loadedPlugin.getId(), loadedPlugin);
        if (existing != null) throw PluginLoadingException.pluginDuplicate(loadedPlugin.getDescriptor(), existing.getDescriptor());
    }

    void addLoadedTargetType(RegisteredTargetType<?> targetType) {
        state.require(State.POPULATING);
        final var existingByName = loadedTargets.putIfAbsent(targetType.getId(), targetType);
        if (existingByName != null) throw PluginLoadingException.targetIdDuplicate(targetType.getDescriptor(), existingByName.getId());
        final var existingByClass = classToTargetMap.putIfAbsent(targetType.getTargetClass(), targetType);
        if (existingByClass != null) throw PluginLoadingException.targetClassDuplicate(targetType.getDescriptor(), existingByClass.getId());
    }

    void addLoadedComponentType(RegisteredComponentType<?, ?> componentType) {
        state.require(State.POPULATING);
        final var existingByName = loadedComponents.putIfAbsent(componentType.getId(), componentType);
        if (existingByName != null) throw PluginLoadingException.componentIdDuplicate(componentType.getDescriptor(), existingByName.getId());
        final var existingByClass = classToComponentMap.putIfAbsent(componentType.getComponentClass(), componentType);
        if (existingByClass != null) throw PluginLoadingException.componentClassDuplicate(componentType.getDescriptor(), existingByClass.getId());
    }

    @Override
    public String toString() {
        return "ClockworkCore{state=" + state + '}';
    }

}
