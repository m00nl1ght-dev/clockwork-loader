package dev.m00nl1ght.clockwork.core;

import org.jetbrains.annotations.NotNull;

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

    protected ClockworkCore(@NotNull ModuleLayer moduleLayer) {
        this.moduleLayer = Objects.requireNonNull(moduleLayer);
    }

    /**
     * Returns an unmodifiable collection of all {@link LoadedPlugin}s.
     */
    public @NotNull Collection<@NotNull LoadedPlugin> getLoadedPlugins() {
        return Collections.unmodifiableCollection(loadedPlugins.values());
    }

    public @NotNull Optional<LoadedPlugin> getLoadedPlugin(@NotNull String pluginId) {
        return Optional.ofNullable(loadedPlugins.get(Objects.requireNonNull(pluginId)));
    }

    public @NotNull LoadedPlugin getLoadedPluginOrThrow(@NotNull String pluginId) {
        final var plugin = loadedPlugins.get(Objects.requireNonNull(pluginId));
        if (plugin == null) throw new RuntimeException("Missing plugin for id: " + pluginId);
        return plugin;
    }

    /**
     * Returns an unmodifiable collection of all loaded {@link TargetType}s.
     */
    public @NotNull Collection<@NotNull RegisteredTargetType<?>> getLoadedTargetTypes() {
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
    public @NotNull TargetType<ClockworkCore> getTargetType() {
        return coreContainer != null ? coreContainer.getTargetType() : getTargetTypeOrThrow(ClockworkCore.class);
    }

    /**
     * Returns the {@link TargetType} for the given target class, wrapped in an {@link Optional}.
     * If no such target is registered to this ClockworkCore, this method will return an empty optional.
     *
     * @param targetClass the class corresponding to the desired TargetType
     */
    @SuppressWarnings("unchecked")
    public <T extends ComponentTarget> @NotNull Optional<RegisteredTargetType<T>> getTargetType(
            @NotNull Class<T> targetClass) {

        final var type = classToTargetMap.get(Objects.requireNonNull(targetClass));
        if (type == null) return Optional.empty();
        return Optional.of((RegisteredTargetType<T>) type);
    }

    @SuppressWarnings("unchecked")
    public <T extends ComponentTarget> @NotNull RegisteredTargetType<T> getTargetTypeOrThrow(
            @NotNull Class<T> targetClass) {

        final var type = classToTargetMap.get(Objects.requireNonNull(targetClass));
        if (type == null) throw new RuntimeException("Missing target type for class: " + targetClass);
        return (RegisteredTargetType<T>) type;
    }

    /**
     * Returns the {@link TargetType} with the given id, wrapped in an {@link Optional}.
     * If no such target is registered to this ClockworkCore, this method will return an empty optional.
     *
     * @param targetId the id of the desired TargetType
     */
    public @NotNull Optional<RegisteredTargetType<?>> getTargetType(@NotNull String targetId) {
        return Optional.ofNullable(loadedTargets.get(Objects.requireNonNull(targetId)));
    }

    public @NotNull RegisteredTargetType<?> getTargetTypeOrThrow(@NotNull String targetId) {
        final var target = loadedTargets.get(Objects.requireNonNull(targetId));
        if (target == null) throw new RuntimeException("Missing target type for id: " + targetId);
        return target;
    }

    /**
     * Returns an unmodifiable collection of all loaded {@link ComponentType}s.
     */
    public @NotNull Collection<@NotNull RegisteredComponentType<?, ?>> getLoadedComponentTypes() {
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
    public <C, T extends ComponentTarget> @NotNull Optional<RegisteredComponentType<C, T>> getComponentType(
            @NotNull Class<C> componentClass,
            @NotNull Class<T> targetClass) {

        final var type = classToComponentMap.get(componentClass);
        if (type == null || type.getTargetType().getTargetClass() != targetClass) {
            return Optional.empty();
        } else {
            return Optional.of((RegisteredComponentType<C, T>) type);
        }
    }

    @SuppressWarnings("unchecked")
    public <C, T extends ComponentTarget> @NotNull RegisteredComponentType<C, T> getComponentTypeOrThrow(
            @NotNull Class<C> componentClass,
            @NotNull Class<T> targetClass) {

        final var type = getComponentTypeOrThrow(componentClass);
        final var actual = type.getTargetType().getTargetClass();
        if (actual != targetClass) {
            final var msg = "Component type " + type + " has target " + actual + " but expected " + targetClass;
            throw new RuntimeException(msg);
        } else {
            return (RegisteredComponentType<C, T>) type;
        }
    }

    /**
     * Returns the {@link ComponentType} for the given component class, wrapped in an {@link Optional}.
     * If no such component is registered to this ClockworkCore, this method will return an empty optional.
     *
     * @param componentClass the class corresponding to the desired ComponentType
     */
    @SuppressWarnings("unchecked")
    public <C> @NotNull Optional<RegisteredComponentType<C, ?>> getComponentType(
            @NotNull Class<C> componentClass) {

        final var type = classToComponentMap.get(componentClass);
        if (type == null) return Optional.empty();
        return Optional.of((RegisteredComponentType<C, ?>) type);
    }

    @SuppressWarnings("unchecked")
    public <C> @NotNull RegisteredComponentType<C, ?> getComponentTypeOrThrow(
            @NotNull Class<C> componentClass) {

        final var type = classToComponentMap.get(componentClass);
        if (type == null) throw new RuntimeException("Missing component type for class: " + componentClass);
        return (RegisteredComponentType<C, ?>) type;
    }

    /**
     * Returns the {@link ComponentType} with the given id, wrapped in an {@link Optional}.
     * If no such component is registered to this ClockworkCore, this method will return an empty optional.
     *
     * @param componentId the id of the desired ComponentType
     */
    public @NotNull Optional<RegisteredComponentType<?, ?>> getComponentType(@NotNull String componentId) {
        return Optional.ofNullable(loadedComponents.get(componentId));
    }

    public @NotNull RegisteredComponentType<?, ?> getComponentTypeOrThrow(@NotNull String componentId) {
        final var component = loadedComponents.get(componentId);
        if (component == null) throw new RuntimeException("Missing component type for id: " + componentId);
        return component;
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

    public @NotNull ModuleLayer getModuleLayer() {
        return moduleLayer;
    }

    /**
     * Returns the internal state of this ClockworkCore.
     */
    public @NotNull State getState() {
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

        public void require(@NotNull State state) {
            if (this != state)
                throw new IllegalStateException("Required state [" + state + "], but was [" + this + "]");
        }

        public void requireAfter(@NotNull State state) {
            if (this.ordinal() <= state.ordinal())
                throw new IllegalStateException("Required state after [" + state + "], but was [" + this + "]");
        }

        public void requireBefore(@NotNull State state) {
            if (this.ordinal() >= state.ordinal())
                throw new IllegalStateException("Required state before [" + state + "], but was [" + this + "]");
        }

        public void requireOrAfter(@NotNull State state) {
            if (this.ordinal() < state.ordinal())
                throw new IllegalStateException("Required state [" + state + "] or after, but was [" + this + "]");
        }

        public void requireOrBefore(@NotNull State state) {
            if (this.ordinal() > state.ordinal())
                throw new IllegalStateException("Required state [" + state + "] or before, but was [" + this + "]");
        }

    }

    // ### Internal ###

    void setState(@NotNull State state) {
        Objects.requireNonNull(state);
        state.requireOrAfter(this.state);
        this.state = state;
    }

    void setCoreContainer(@NotNull ComponentContainer<ClockworkCore> container) {
        Objects.requireNonNull(container);
        if (this.coreContainer != null) throw new IllegalStateException();
        this.coreContainer = container;
    }

    void addLoadedPlugin(@NotNull LoadedPlugin loadedPlugin) {
        state.require(State.POPULATING);
        final var existing = loadedPlugins.putIfAbsent(loadedPlugin.getId(), loadedPlugin);
        if (existing != null) throw PluginLoadingException.pluginDuplicate(loadedPlugin.getDescriptor(), existing.getDescriptor());
    }

    void addLoadedTargetType(@NotNull RegisteredTargetType<?> targetType) {
        state.require(State.POPULATING);
        final var existingByName = loadedTargets.putIfAbsent(targetType.getId(), targetType);
        if (existingByName != null) throw PluginLoadingException.targetIdDuplicate(targetType.getDescriptor(), existingByName.getId());
        final var existingByClass = classToTargetMap.putIfAbsent(targetType.getTargetClass(), targetType);
        if (existingByClass != null) throw PluginLoadingException.targetClassDuplicate(targetType.getDescriptor(), existingByClass.getId());
    }

    void addLoadedComponentType(@NotNull RegisteredComponentType<?, ?> componentType) {
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
