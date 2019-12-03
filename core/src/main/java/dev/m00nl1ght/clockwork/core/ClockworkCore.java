package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.classloading.ModuleManager;
import dev.m00nl1ght.clockwork.debug.DebugProfiler;
import dev.m00nl1ght.clockwork.debug.ProfilingEventListenerFactory;
import dev.m00nl1ght.clockwork.locator.PluginLocator;
import dev.m00nl1ght.clockwork.processor.PluginProcessorManager;
import dev.m00nl1ght.clockwork.resolver.DependencyResolver;
import dev.m00nl1ght.clockwork.util.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class ClockworkCore implements ComponentTarget {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final String CORE_PLUGIN_ID = "clockwork";
    public static final String CORE_TARGET_ID = CORE_PLUGIN_ID + ":core";

    private final Map<String, TargetType<?>> componentTargets = new HashMap<>();
    private final Map<Class<?>, TargetType<?>> classToTargetMap = new HashMap<>();
    private final Map<String, ComponentType<?, ?>> loadedComponents = new HashMap<>();
    private final Map<Class<?>, ComponentType<?, ?>> classToComponentMap = new HashMap<>();
    private final Map<String, PluginContainer> loadedPlugins = new HashMap<>();
    private final PluginProcessorManager processors = new PluginProcessorManager(MethodHandles.lookup());
    private final ModuleManager moduleManager;
    private transient State state = State.CONSTRUCTED;
    private ComponentContainer<ClockworkCore> coreContainer;
    private EventListenerFactory listenerFactory;

    private ClockworkCore(DependencyResolver depResolver, DebugProfiler profiler) {
        listenerFactory = profiler == null ? EventListenerFactory.DEFAULT : new ProfilingEventListenerFactory(profiler);
        moduleManager = new ModuleManager(depResolver.getPluginDefinitions(), ModuleLayer.boot());
        depResolver.getPluginDefinitions().forEach(this::buildPlugin);
        depResolver.getTargetDefinitions().forEach(this::buildComponentTarget);
        depResolver.getComponentDefinitions().forEach(this::buildComponent);
        depResolver.getTargetDefinitions().forEach(t -> componentTargets.get(t.getId()).getPrimer().init());
        this.state = State.LOCATED;
    }

    public static ClockworkCore load(Collection<PluginLocator> locators) {
        return load(locators, null);
    }

    public static ClockworkCore load(Collection<PluginLocator> locators, DebugProfiler profiler) {
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

        return new ClockworkCore(depResolver, profiler);
    }

    public synchronized void init() {
        if (this.state != State.LOCATED) throw new IllegalStateException();
        final var coreTarget = getTargetType(ClockworkCore.class);
        if (coreTarget.isEmpty()) throw PluginLoadingException.coreTargetMissing(CORE_TARGET_ID);
        coreContainer = new ComponentContainer<>(coreTarget.get(), this);
        coreContainer.initComponents();
        this.state = State.INITIALISED;
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
        final var component = target.getPrimer().register(def, plugin, compClass);
        final var existingByName = loadedComponents.putIfAbsent(component.getId(), component);
        if (existingByName != null) throw PluginLoadingException.componentIdDuplicate(def, existingByName.getId());
        final var existingByClass = classToComponentMap.putIfAbsent(compClass, component);
        if (existingByClass != null) throw PluginLoadingException.componentClassDuplicate(def, existingByClass.getId());
        processors.apply(component, def.getProcessors());
    }

    private void buildComponentTarget(TargetDefinition def) {
        final var plugin = loadedPlugins.get(def.getPlugin().getId());
        if (plugin == null) throw new IllegalStateException("plugin vanished somehow");
        final var targetClass = moduleManager.loadClassForPlugin(def.getTargetClass(), plugin);
        if (!ComponentTarget.class.isAssignableFrom(targetClass)) throw PluginLoadingException.invalidTargetClass(def);
        final var target = TargetType.create(def, plugin, (Class<? extends ComponentTarget>) targetClass, listenerFactory);
        final var existingByName = componentTargets.putIfAbsent(target.getId(), target);
        if (existingByName != null) throw PluginLoadingException.targetIdDuplicate(def, existingByName.getId());
        final var existingByClass = classToTargetMap.putIfAbsent(targetClass, target);
        if (existingByClass != null) throw PluginLoadingException.targetClassDuplicate(def, existingByClass.getId());
        processors.apply(target, def.getProcessors());
    }

    public <T extends ComponentTarget> Optional<TargetType<T>> getTargetType(Class<T> targetClass) {
        final var type = classToTargetMap.get(targetClass);
        if (type == null) return Optional.empty();
        return Optional.of((TargetType<T>) type);
    }

    public Optional<TargetType<?>> getTargetType(String targetId) {
        return Optional.ofNullable(componentTargets.get(targetId));
    }

    public <C, T extends ComponentTarget> Optional<ComponentType<C, T>> getComponentType(Class<C> componentClass, Class<T> targetClass) {
        final var type = classToComponentMap.get(componentClass);
        if (type == null) return Optional.empty();
        if (type.getTargetType().getTargetClass() != targetClass) return Optional.empty();
        return Optional.of((ComponentType<C, T>) type);
    }

    public <C> Optional<ComponentType<C, ?>> getComponentType(Class<C> componentClass) {
        final var type = classToComponentMap.get(componentClass);
        if (type == null) return Optional.empty();
        return Optional.of((ComponentType<C, ?>) type);
    }

    public Optional<ComponentType<?, ?>> getComponentType(String componentId) {
        return Optional.ofNullable(loadedComponents.get(componentId));
    }

    @Override
    public Object getComponent(int internalID) {
        return coreContainer == null ? null : coreContainer.getComponent(internalID);
    }

    @Override
    public TargetType<?> getTargetType() {
        return coreContainer == null ? null : coreContainer.getTargetType();
    }

    private synchronized void rebuildEventListeners() {
        for (var target : this.componentTargets.values()) {
            target.rebuildEventListeners(listenerFactory);
        }
    }

    public void enableProfiler(DebugProfiler profiler) {
        Preconditions.notNull(profiler, "profiler");
        this.listenerFactory = new ProfilingEventListenerFactory(profiler);
        rebuildEventListeners();
    }

    public void disableProfiler() {
        this.listenerFactory = EventListenerFactory.DEFAULT;
        rebuildEventListeners();
    }

    public State getState() {
        return state;
    }

    public enum State {
        CONSTRUCTED, LOCATED, INITIALISED
    }

}
