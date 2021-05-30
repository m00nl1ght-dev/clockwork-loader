package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.container.ImmutableComponentContainer;
import dev.m00nl1ght.clockwork.core.ClockworkCore.State;
import dev.m00nl1ght.clockwork.descriptor.ComponentDescriptor;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.descriptor.TargetDescriptor;
import dev.m00nl1ght.clockwork.fnder.ModuleLayerPluginFinder;
import dev.m00nl1ght.clockwork.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.interfaces.impl.ComponentInterfaceImplExact;
import dev.m00nl1ght.clockwork.jigsaw.JigsawStrategy;
import dev.m00nl1ght.clockwork.logger.Logger;
import dev.m00nl1ght.clockwork.logger.impl.SysOutLogging;
import dev.m00nl1ght.clockwork.reader.ManifestPluginReader;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.ReflectionUtil;
import dev.m00nl1ght.clockwork.util.TopologicalSorter;
import dev.m00nl1ght.clockwork.version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The entry point of the plugin loading framework.
 *
 * From application code, call {@link ClockworkLoader#build}
 * to get a ClockworkLoader instance.
 */
public final class ClockworkLoader {

    private static final Logger LOGGER = Logger.create("Clockwork-Loader");
    private static final Lookup INTERNAL_LOOKUP = MethodHandles.lookup();

    private static final int TARGET_JAVA_VERSION = 14;

    static {
        final var version = Runtime.version();
        if (version.feature() != TARGET_JAVA_VERSION)
            LOGGER.warn("The current Java version [] is not fully supported. " +
                    "CWL was developed for Java []. Using any other version can cause instability and crashes.",
                    version, TARGET_JAVA_VERSION);
        if (ClockworkLoader.class.getModule().getName() == null)
            throw FormatUtil.rtExc("Core module was not loaded correctly (the module is unnamed)");
        if (LOGGER instanceof SysOutLogging.SysOutLogger)
            LOGGER.warn("No supported logging framework detected. Printing logs to SOUT.");
    }

    public static @NotNull ClockworkLoader build(@NotNull ClockworkConfig config) {
        Objects.requireNonNull(config);
        return new ClockworkLoader(null, config);
    }

    public static @NotNull ClockworkLoader build(@NotNull ClockworkCore parent, @NotNull ClockworkConfig config) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(parent).getState().requireOrAfter(State.INITIALISED);
        return new ClockworkLoader(parent, config);
    }

    public static @NotNull ClockworkLoader buildBootLayerDefault() {
        final var configBuilder = ClockworkConfig.builder();
        configBuilder.addPluginReader(ManifestPluginReader.newConfig("manifest"));
        configBuilder.addPluginFinder(ModuleLayerPluginFinder.configBuilder("boot").wildcard().build());
        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("clockwork"));
        return build(configBuilder.build());
    }

    private final ClockworkCore parent;
    private final ClockworkConfig config;

    private ClockworkCore core;

    private final ExtensionContext extensionContext = new ExtensionContext(true);
    private final List<PluginLoadingProblem> fatalProblems = new ArrayList<>();
    private final List<PluginLoadingProblem> skippedProblems = new ArrayList<>();

    private ClockworkLoader(@Nullable ClockworkCore parent, @NotNull ClockworkConfig config) {
        this.parent = parent;
        this.config = config;
    }

    public synchronized void collectExtensionsFromParent() {
        if (parent != null) {
            final var extInterface = new ComponentInterfaceImplExact<>(ClockworkExtension.class, parent.getCoreTargetType());
            extInterface.apply(parent, e -> e.registerFeatures(extensionContext));
        }
    }

    public @NotNull ExtensionContext getExtensionContext() {
        return extensionContext;
    }

    public @NotNull List<@NotNull PluginLoadingProblem> getFatalProblems() {
        return Collections.unmodifiableList(fatalProblems);
    }

    public @NotNull List<@NotNull PluginLoadingProblem> getSkippedProblems() {
        return Collections.unmodifiableList(skippedProblems);
    }

    public @NotNull ClockworkCore loadAndInit() {
        this.load();
        this.init();
        return core;
    }

    public synchronized @NotNull ClockworkCore load() {

        if (core != null) throw new IllegalStateException("Already loaded");

        final var pluginReferences = new LinkedList<PluginReference>();
        final var componentSorter = new ComponentSorter();
        final var targetSorter = new TargetSorter();

        // If there is a parent core, add it's components and targets to the sorters first.
        if (parent != null) {
            for (final var c : parent.getLoadedComponentTypes()) componentSorter.add(c.getDescriptor());
            for (final var t : parent.getLoadedTargetTypes()) targetSorter.add(t.getDescriptor());
        }

        // Compile a map of all plugins wanted by the config.
        final var wantedPlugins = config.getWantedPlugins().stream()
                .collect(Collectors.toMap(DependencyDescriptor::getPlugin, Function.identity(), (a, b) -> b, HashMap::new));

        // Build the LoadingContext and add plugins found by wildcard finders.
        final var loadingContext = LoadingContext.of(config, extensionContext);
        for (final var config : config.getFinders()) {
            if (config.isWildcard()) {
                final var finder = loadingContext.getFinder(config.getName());
                for (final var plugin : finder.getAvailablePlugins(loadingContext)) {
                    wantedPlugins.computeIfAbsent(plugin, DependencyDescriptor::buildAnyVersion);
                }
            }
        }

        // Now try to find all the wanted plugins.
        for (final var wanted : wantedPlugins.values()) {

            // If the parent has it and the version matches, it doesn't have to be located again.
            if (parent != null) {
                final var inherited = parent.getLoadedPlugin(wanted.getPlugin());
                if (inherited.isPresent()) {
                    if (!wanted.acceptsVersion(inherited.get().getDescriptor().getVersion()))
                        addProblem(PluginLoadingProblem.inheritedVersionClash(wanted, inherited.get().getDescriptor()));
                    continue;
                }
            }

            // Otherwise, try to find it using the PluginFinders from the config.
            final var found = new HashMap<Version, PluginFinder>();
            for (var finder : loadingContext.getFinders()) {
                for (final var version : finder.getAvailableVersions(loadingContext, wanted.getPlugin())) {
                    if (wanted.acceptsVersion(version)) found.putIfAbsent(version, finder);
                }
            }

            // If anything was found, add it to the sorters.
            final var bestMatch = found.keySet().stream().max(Comparator.naturalOrder());
            if (bestMatch.isEmpty()) {
                addProblem(PluginLoadingProblem.pluginNotFound(wanted));
            } else {
                final var finder = found.get(bestMatch.get());
                final var ref = finder.find(loadingContext, wanted.getPlugin(), bestMatch.get())
                        .orElseThrow(() -> FormatUtil.rtExc("PluginFinder [] failed to find []", finder, wanted));
                LOGGER.info("Plugin [] was located by []", ref, finder);
                loadingContext.getVerifiers().forEach(v -> v.verifyPlugin(ref));
                pluginReferences.addLast(ref);
                ref.getDescriptor().getComponentDescriptors().forEach(componentSorter::add);
                ref.getDescriptor().getTargetDescriptors().forEach(targetSorter::add);
            }

        }

        // These will contain the sorted components and targets.
        final var componentDescriptors = new LinkedList<ComponentDescriptor>();
        final var targetDescriptors = new LinkedList<TargetDescriptor>();

        // Now trigger the sorters.
        componentSorter.sort(componentDescriptors);
        targetSorter.sort(targetDescriptors);

        // If there were any fatal problems, print them and throw an exception.
        if (!fatalProblems.isEmpty()) {
            LOGGER.error("The following fatal problems occurred while resolving dependencies:");
            for (var p : fatalProblems) LOGGER.error(p.format());
            throw PluginLoadingException.fatalLoadingProblems(fatalProblems);
        }

        // If there were any other problems, just print them.
        if (!skippedProblems.isEmpty()) {
            LOGGER.info("The following optional components have been skipped because requirements are not met:");
            for (var p : skippedProblems) LOGGER.info(p.format());
        }

        // Create the new ModuleLayer and the ClockworkCore instance.
        final var layerMap = buildModuleLayers(pluginReferences);
        final var moduleLayers = layerMap.values().stream().distinct().collect(Collectors.toList());
        core = new ClockworkCore(moduleLayers);

        // First add the plugins inherited from the parent.
        if (parent != null) {
            for (final var inherited : parent.getLoadedPlugins()) {
                final var plugin = new LoadedPlugin(inherited.getDescriptor(), core, inherited.getMainModule());
                core.addLoadedPlugin(plugin);
            }
        }

        // Then add the new ones that were located using the config.
        for (final var pluginReference : pluginReferences) {

            // Find the main module of the plugin, build the LoadedPlugin object and add it to the core.
            final var layer = layerMap.get(pluginReference);
            if (layer == null) throw FormatUtil.rtExc("No module layer present for plugin []", pluginReference);
            final var mainModule = layer.findModule(pluginReference.getModuleName()).orElseThrow();
            final var plugin = new LoadedPlugin(pluginReference.getDescriptor(), core, mainModule);
            core.addLoadedPlugin(plugin);

        }

        // Next, prepare and add all targets provided by the plugins.
        for (final var targetDescriptor : targetDescriptors) {

            // Get the plugin that is providing this target.
            final var plugin = core.getLoadedPluginOrThrow(targetDescriptor.getPluginId());

            // If the parent has it, get the target class from there.
            if (parent != null) {
                final var inherited = parent.getTargetType(targetDescriptor.getId());
                if (inherited.isPresent()) {
                    buildTarget(plugin, targetDescriptor, inherited.get().getTargetClass());
                    continue;
                }
            }

            // Otherwise, get the target class from the ModuleLayer, then verify and cast it.
            final var targetClass = loadClassForPlugin(targetDescriptor.getTargetClass(), plugin);
            if (!ComponentTarget.class.isAssignableFrom(targetClass))
                throw PluginLoadingException.invalidTargetClass(targetDescriptor, targetClass);

            @SuppressWarnings("unchecked")
            final var targetCasted = (Class<? extends ComponentTarget>) targetClass;
            buildTarget(plugin, targetDescriptor, targetCasted);

        }

        // Now, prepare and add all components provided by the plugins.
        for (final var componentDescriptor : componentDescriptors) {

            // Get the plugin that is providing this component, and the target it is for.
            final var plugin = core.getLoadedPluginOrThrow(componentDescriptor.getPluginId());
            final var target = core.getTargetType(componentDescriptor.getTargetId());
            if (target.isEmpty()) throw PluginLoadingException.componentMissingTarget(componentDescriptor);

            // If the parent has it, get the component class from there.
            if (parent != null) {
                final var inherited = parent.getComponentType(componentDescriptor.getId());
                if (inherited.isPresent()) {
                    final var componentClass = inherited.get().getComponentClass();
                    buildComponent(plugin, componentDescriptor, target.get(), componentClass);
                    continue;
                }
            }

            // Otherwise, get the component class from the ModuleLayer.
            final var componentClass = loadClassForPlugin(componentDescriptor.getComponentClass(), plugin);
            buildComponent(plugin, componentDescriptor, target.get(), componentClass);

        }

        // Make sure all plugins have a valid main component.
        for (final var plugin : core.getLoadedPlugins()) {
            plugin.getMainComponent();
        }

        // The core now contains all the loaded plugins, targets and components.
        core.setState(State.POPULATED);

        // Notify all registered plugin processors.
        for (final var entry : extensionContext.getProcessorRegistry().getRegistered().entrySet()) {
            try {
                entry.getValue().onLoadingStart(core, parent);
            } catch (Throwable t) {
                throw PluginLoadingException.inProcessor(entry.getKey(), t);
            }
        }

        // Apply the plugin processors defined to each plugin respectively.
        applyPluginProcessors();

        // Initialise the target types.
        for (final var targetType : core.getLoadedTargetTypes()) {
            targetType.init();
        }

        // Warn if any component type has no factory assigned.
        for (final var componentType : core.getLoadedComponentTypes()) {
            if (componentType.getFactoryInternal() == ComponentFactory.EMPTY) {
                LOGGER.warn("No factory or valid constructor available for component type []", componentType);
            }
        }

        // The core is now ready for use.
        core.setState(State.PROCESSED);
        return core;
    }

    private void applyPluginProcessors() {
        for (final var plugin : core.getLoadedPlugins()) {

            // Get the processors, and skip the plugin if there are none.
            final var processors = plugin.getDescriptor().getProcessors();
            if (processors.isEmpty()) continue;

            // Now apply the processors.
            final var context = new PluginProcessorContext(plugin, INTERNAL_LOOKUP);
            for (var name : processors) {

                final var optional = name.startsWith("?");
                if (optional) name = name.substring(1);

                final var processor = extensionContext.getProcessorRegistry().get(name);
                if (processor == null) {
                    if (!optional) throw PluginLoadingException.missingProcessor(plugin.getId(), name);
                } else {
                    try {
                        if (core.getState() == State.INITIALISED) {
                            processor.processLate(context);
                        } else {
                            processor.processEarly(context);
                        }
                    } catch (Throwable t) {
                        throw PluginLoadingException.inProcessor(plugin, name, t);
                    }
                }
            }
        }
    }

    /**
     * Constructs new ModuleLayers for the specific set of plugin definitions,
     * using the configured {@link JigsawStrategy}.
     * This is called during plugin loading, after all definitions have been located,
     * but before any components or classes are loaded.
     */
    private @NotNull Map<@NotNull PluginReference, @NotNull ModuleLayer>
    buildModuleLayers(@NotNull Collection<@NotNull PluginReference> plugins) {
        try {
            final var jigsawConfig = config.getJigsawStrategy();
            final var strategyType = extensionContext.getJigsawTypeRegistry().get(jigsawConfig.getType());
            final var strategy = strategyType.build(jigsawConfig);
            return strategy.buildModuleLayers(plugins, config.getLibModulePath(), parent);
        } catch (Exception e) {
            throw PluginLoadingException.resolvingModules(e, null);
        }
    }

    private <T extends ComponentTarget> void buildTarget(
            @NotNull LoadedPlugin plugin,
            @NotNull TargetDescriptor descriptor,
            @NotNull Class<T> targetClass) {

        // First, fetch the parent target if there is any, and verify it.
        TargetType<? super T> parentType = null;
        if (descriptor.getParent() != null) {
            final var found = plugin.getClockworkCore().getTargetType(descriptor.getParent());
            if (found.isEmpty()) throw PluginLoadingException.targetMissingParent(descriptor);
            if (found.get().getTargetClass().isAssignableFrom(targetClass)) {
                @SuppressWarnings("unchecked")
                final var casted = (TargetType<? super T>) found.get();
                parentType = casted;
            } else {
                throw PluginLoadingException.invalidParentForTarget(descriptor, found.get());
            }
        }

        // Construct the new TargetType.
        final var target = new RegisteredTargetType<>(plugin, parentType, descriptor, targetClass);

        // Then add it to the core and plugin.
        plugin.getClockworkCore().addLoadedTargetType(target);
        plugin.addLoadedTargetType(target);

    }

    private <T extends ComponentTarget> void buildComponent(
            @NotNull LoadedPlugin plugin,
            @NotNull ComponentDescriptor descriptor,
            @NotNull RegisteredTargetType<T> targetType,
            @NotNull Class<?> componentClass) {

        // Verify component class and generic type.
        if (!ReflectionUtil.tryFindSupertype(componentClass, Component.class, targetType.getTargetClass()))
            throw PluginLoadingException.invalidComponentClass(descriptor, componentClass);

        // Cast the component class to its generic type.
        @SuppressWarnings("unchecked")
        final var compCasted = (Class<? extends Component<T>>) componentClass;

        // Construct the new ComponentType.
        final var component = new RegisteredComponentType<>(plugin, descriptor, targetType, compCasted);

        // Then add it to the core and plugin.
        plugin.getClockworkCore().addLoadedComponentType(component);
        plugin.addLoadedComponentType(component);

    }

    /**
     * Loads the class with the given name from the internal module layer or any parent layers
     * and verifies that it was loaded from the main module of the given plugin.
     *
     * @param className the qualified name of the class to be loaded
     * @param plugin    the plugin the class should be loaded from
     * @throws PluginLoadingException if the class is in a module other than
     *                                the main module of the plugin, or the class was not found
     */
    public static @NotNull Class<?> loadClassForPlugin(@NotNull String className, @NotNull LoadedPlugin plugin) {
        try {
            final var clazz = Class.forName(className, false, plugin.getMainModule().getClassLoader());
            if (clazz.getModule() != plugin.getMainModule())
                throw PluginLoadingException.pluginClassIllegal(clazz, plugin);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw PluginLoadingException.pluginClassNotFound(className, plugin.getDescriptor());
        }
    }

    /**
     * Initialises this ClockworkCore with a core container.
     * The core container is created for target id {@code clockwork:core}.
     * The core container is a special {@link ComponentContainer} that is attached to the ClockworkCore itself.
     * It will store all plugin components which exist in a static context
     * and are not attached to individual objects within the application.
     * For example, this includes the main component of each plugin.
     */
    public synchronized void init() {

        if (core == null) throw new IllegalStateException("Not loaded yet");
        core.getState().require(State.PROCESSED);

        // Get the core target.
        final var coreTarget = core.getTargetTypeOrThrow(ClockworkCore.class);

        // Build the component container and set it.
        final var container = new ImmutableComponentContainer(coreTarget, core);
        core.setCoreContainer(container);

        // Init the components and update the state of the core.
        container.initComponents();
        core.setState(State.INITIALISED);

        // Apply the plugin processors defined to each plugin respectively.
        applyPluginProcessors();

        // Notify all registered plugin processors.
        for (final var entry : extensionContext.getProcessorRegistry().getRegistered().entrySet()) {
            try {
                entry.getValue().onLoadingComplete(core);
            } catch (Throwable t) {
                throw PluginLoadingException.inProcessor(entry.getKey(), t);
            }
        }

    }

    private void addProblem(@NotNull PluginLoadingProblem problem) {
        (problem.isFatal() ? fatalProblems : skippedProblems).add(problem);
    }

    static @NotNull Lookup getInternalLookup() {
        return INTERNAL_LOOKUP;
    }

    private class ComponentSorter extends TopologicalSorter<ComponentDescriptor, DependencyDescriptor> {

        @Override
        public String idFor(ComponentDescriptor obj) {
            return obj.getId();
        }

        @Override
        public String idOfDep(DependencyDescriptor obj) {
            return obj.getTarget();
        }

        @Override
        public boolean isDepSatisfied(ComponentDescriptor node, DependencyDescriptor dep, ComponentDescriptor present) {
            return dep.acceptsVersion(present.getVersion());
        }

        @Override
        public Iterable<DependencyDescriptor> depsFor(ComponentDescriptor obj) {
            return obj.getDependencies();
        }

        @Override
        protected void onDuplicateId(ComponentDescriptor node, ComponentDescriptor present) {
            addProblem(PluginLoadingProblem.duplicateIdFound(node.getPluginId(), node, present));
        }

        @Override
        public void onCycleFound(ComponentDescriptor tail) {
            addProblem(PluginLoadingProblem.depCycleFound(tail.getPluginId(), tail));
        }

        @Override
        public void onMissingDep(ComponentDescriptor node, DependencyDescriptor dep, ComponentDescriptor present) {
            addProblem(PluginLoadingProblem.depNotFound(node, dep, present));
        }

        @Override
        public void onSkippedDep(ComponentDescriptor node, ComponentDescriptor present) {
            addProblem(PluginLoadingProblem.depSkipped(node, present));
        }

    }

    private class TargetSorter extends TopologicalSorter<TargetDescriptor, String> {

        @Override
        public String idFor(TargetDescriptor obj) {
            return obj.getId();
        }

        @Override
        public String idOfDep(String obj) {
            return obj;
        }

        @Override
        public boolean isDepSatisfied(TargetDescriptor node, String dep, TargetDescriptor present) {
            return true;
        }

        @Override
        public Iterable<String> depsFor(TargetDescriptor obj) {
            return obj.getParent() == null ? Collections.emptySet() : Collections.singleton(obj.getParent());
        }

        @Override
        protected void onDuplicateId(TargetDescriptor node, TargetDescriptor present) {
            addProblem(PluginLoadingProblem.duplicateIdFound(node.getPluginId(), node, present));
        }

        @Override
        public void onCycleFound(TargetDescriptor tail) {
            addProblem(PluginLoadingProblem.depCycleFound(tail.getPluginId(), tail));
        }

        @Override
        public void onMissingDep(TargetDescriptor node, String required, TargetDescriptor present) {
            addProblem(PluginLoadingProblem.parentNotFound(node));
        }

        @Override
        public void onSkippedDep(TargetDescriptor node, TargetDescriptor present) {
            addProblem(PluginLoadingProblem.parentNotFound(node));
        }

    }

}
