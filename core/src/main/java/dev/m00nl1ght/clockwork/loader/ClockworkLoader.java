package dev.m00nl1ght.clockwork.loader;

import dev.m00nl1ght.clockwork.component.ComponentContainer;
import dev.m00nl1ght.clockwork.component.ComponentFactory;
import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.TargetType;
import dev.m00nl1ght.clockwork.component.impl.SimpleComponentContainer;
import dev.m00nl1ght.clockwork.core.*;
import dev.m00nl1ght.clockwork.core.ClockworkCore.Phase;
import dev.m00nl1ght.clockwork.descriptor.ComponentDescriptor;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.descriptor.TargetDescriptor;
import dev.m00nl1ght.clockwork.interfaces.ComponentInterface;
import dev.m00nl1ght.clockwork.loader.classloading.ClassTransformer;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.impl.ModuleLayerPluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.impl.ModulePathPluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.impl.NestedPluginFinder;
import dev.m00nl1ght.clockwork.loader.jigsaw.JigsawStrategy;
import dev.m00nl1ght.clockwork.loader.jigsaw.impl.JigsawStrategyFlat;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.loader.reader.impl.ManifestPluginReader;
import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ConfiguredFeatureProviders;
import dev.m00nl1ght.clockwork.utils.config.ConfiguredFeatures;
import dev.m00nl1ght.clockwork.utils.logger.Logger;
import dev.m00nl1ght.clockwork.utils.reflect.ReflectionUtil;
import dev.m00nl1ght.clockwork.utils.version.Version;
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
public final class ClockworkLoader implements ComponentTarget {

    private static final Logger LOGGER = Logger.getLogger("Clockwork-Loader");
    private static final Lookup INTERNAL_LOOKUP = MethodHandles.lookup();

    private static final int TARGET_JAVA_VERSION = 14;

    static {
        final var version = Runtime.version();
        if (version.feature() != TARGET_JAVA_VERSION)
            LOGGER.warn("The current Java version [] is not fully supported. " +
                    "CWL was developed for Java []. Using any other version can cause instability and crashes.",
                    version, TARGET_JAVA_VERSION);
        if (ClockworkLoader.class.getModule().getName() == null)
            throw ClockworkException.generic("Core module was not loaded correctly (the module is unnamed)");
        if (Logger.isFallbackMode())
            LOGGER.warn("No supported logging framework detected. Printing logs to SYSOUT.");
    }

    public static @NotNull ClockworkLoader build(@NotNull Config config) {
        Objects.requireNonNull(config);
        return new ClockworkLoader(null, config, TargetType.empty(ClockworkLoader.class));
    }

    public static @NotNull ClockworkLoader build(@NotNull ClockworkCore parent, @NotNull Config config) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(parent).getPhase().requireOrAfter(Phase.INITIALISED);
        final var targetType = parent.getTargetTypeOrThrow(ClockworkLoader.class);
        return new ClockworkLoader(parent, config, targetType);
    }

    public static @NotNull ClockworkLoader buildBootLayerDefault() {
        final var config = Config.newConfig(ClockworkConfig.SPEC);
        config.put(ClockworkConfig.SPEC.PLUGIN_READERS, List.of(PluginReader.DEFAULT));
        config.put(ClockworkConfig.SPEC.PLUGIN_FINDERS, List.of(ModuleLayerPluginFinder.newConfig("boot", true)));
        config.put(ClockworkConfig.SPEC.WANTED_PLUGINS, List.of(DependencyDescriptor.buildAnyVersion("clockwork")));
        return build(config);
    }

    private final ClockworkCore parent;
    private final Config config;

    private ClockworkCore.Controller controller;
    private ClockworkCore core;

    private final SimpleComponentContainer extensionContainer;
    private final ComponentInterface<LoaderExtension, ClockworkLoader> extensions;
    private final ConfiguredFeatureProviders<ClockworkLoader> featureProviders = new ConfiguredFeatureProviders<>();
    private final ConfiguredFeatures features = new ConfiguredFeatures();

    private final List<PluginLoadingProblem> fatalProblems = new ArrayList<>();
    private final List<PluginLoadingProblem> skippedProblems = new ArrayList<>();

    private ClockworkLoader(@Nullable ClockworkCore parent,
                            @NotNull Config config,
                            @NotNull TargetType<ClockworkLoader> targetType) {
        this.parent = parent;
        this.config = config.copy();
        this.extensions = ComponentInterface.of(LoaderExtension.class, targetType);
        this.extensionContainer = new SimpleComponentContainer(targetType, this);
        this.extensionContainer.initComponents();
    }

    private void registerDefaultExtensionFeatures() {
        ManifestPluginReader.registerTo(this);
        ModuleLayerPluginFinder.registerTo(this);
        ModulePathPluginFinder.registerTo(this);
        NestedPluginFinder.registerTo(this);
        JigsawStrategyFlat.registerTo(this);
    }

    public @NotNull ConfiguredFeatureProviders<ClockworkLoader> getFeatureProviders() {
        return featureProviders;
    }

    public @NotNull ConfiguredFeatures getFeatures() {
        return features;
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

        // Register feature providers from core and from extensions.
        registerDefaultExtensionFeatures();
        extensions.apply(this, LoaderExtension::registerFeatures);
        featureProviders.lock();

        // Build all features defined in the config.
        features.addAll(PluginFinder.class, featureProviders, config.get(ClockworkConfig.SPEC.PLUGIN_FINDERS), this);
        features.addAll(PluginReader.class, featureProviders, config.get(ClockworkConfig.SPEC.PLUGIN_READERS), this);
        features.addAll(ClassTransformer.class, featureProviders, config.get(ClockworkConfig.SPEC.CLASS_TRANSFORMERS), this);
        extensions.apply(this, LoaderExtension::initFeatures);
        features.lock();

        final var pluginReferences = new LinkedList<PluginReference>();
        final var componentSorter = new ComponentSorter();
        final var targetSorter = new TargetSorter();

        // If there is a parent core, add its components and targets to the sorters first.
        if (parent != null) {
            for (final var c : parent.getLoadedComponentTypes()) componentSorter.add(c.getDescriptor());
            for (final var t : parent.getLoadedTargetTypes()) targetSorter.add(t.getDescriptor());
        }

        // Compile a map of all plugins wanted by the config.
        final var wantedPlugins = config.get(ClockworkConfig.SPEC.WANTED_PLUGINS).stream()
                .collect(Collectors.toMap(DependencyDescriptor::getPlugin, Function.identity(), (a, b) -> b, HashMap::new));

        // Add plugins found by wildcard finders.
        for (final var finder : features.getAll(PluginFinder.class)) {
            if (finder.isWildcard()) {
                for (final var plugin : finder.getAvailablePlugins(this)) {
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
            for (var finder : features.getAll(PluginFinder.class)) {
                for (final var version : finder.getAvailableVersions(this, wanted.getPlugin())) {
                    if (wanted.acceptsVersion(version)) found.putIfAbsent(version, finder);
                }
            }

            // If anything was found, add it to the sorters.
            final var bestMatch = found.keySet().stream().max(Comparator.naturalOrder());
            if (bestMatch.isEmpty()) {
                addProblem(PluginLoadingProblem.pluginNotFound(wanted));
            } else {
                final var finder = found.get(bestMatch.get());
                final var ref = finder.find(this, wanted.getPlugin(), bestMatch.get())
                        .orElseThrow(() -> ClockworkException.generic("PluginFinder [] failed to find []", finder, wanted));
                LOGGER.info("Plugin [] was located by []", ref, finder);
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

        // Collect and sort class transformers.
        final var transformers = features.getAll(ClassTransformer.class).stream()
                .sorted(Comparator.comparing(ClassTransformer::priority))
                .collect(Collectors.toList());

        // Create the new ModuleLayer and the ClockworkCore instance.
        final var layerMap = buildModuleLayers(pluginReferences, transformers);
        final var moduleLayers = layerMap.values().stream().distinct().collect(Collectors.toList());
        controller = ClockworkCore.create(moduleLayers);
        core = controller.getCore();

        // Notify extensions about start of first phase.
        extensions.apply(this, LoaderExtension::onCoreConstructed);

        // First add the plugins inherited from the parent.
        if (parent != null) {
            for (final var inherited : parent.getLoadedPlugins()) {
                final var plugin = controller.definePlugin(inherited.getDescriptor(), inherited.getMainModule());
                controller.addLoadedPlugin(plugin);
            }
        }

        // Then add the new ones that were located using the config.
        for (final var pluginReference : pluginReferences) {

            // Find the main module of the plugin, build the LoadedPlugin object and add it to the core.
            final var layer = layerMap.get(pluginReference);
            if (layer == null) throw ClockworkException.generic("No module layer present for plugin []", pluginReference);
            final var mainModule = layer.findModule(pluginReference.getModuleName()).orElseThrow();
            final var plugin = controller.definePlugin(pluginReference.getDescriptor(), mainModule);
            controller.addLoadedPlugin(plugin);

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

        // The core now contains all the loaded plugins, targets and components.
        controller.setState(Phase.POPULATED);
        extensions.apply(this, LoaderExtension::onCorePopulated);

        // Make sure all plugins have a valid main component.
        for (final var plugin : core.getLoadedPlugins()) {
            plugin.getMainComponent();
        }

        // Lock all target types.
        for (final var targetType : core.getLoadedTargetTypes()) {
            targetType.lock();
        }

        // If any component type has no factory assigned, try creating a default one.
        for (final var componentType : core.getLoadedComponentTypes()) {
            if (componentType.getFactory() == ComponentFactory.EMPTY) {
                if (!controller.createDefaultComponentFactory(componentType, INTERNAL_LOOKUP)) {
                    LOGGER.warn("No factory or valid constructor available for component type []", componentType);
                }
            }
        }

        // The core is now ready for use.
        controller.setState(Phase.PROCESSED);
        extensions.apply(this, LoaderExtension::onCoreProcessed);

        return core;
    }

    /**
     * Constructs new ModuleLayers for the specific set of plugin definitions,
     * using the configured {@link JigsawStrategy}.
     * This is called during plugin loading, after all definitions have been located,
     * but before any components or classes are loaded.
     */
    private @NotNull Map<@NotNull PluginReference, @NotNull ModuleLayer> buildModuleLayers(
            @NotNull Collection<@NotNull PluginReference> plugins,
            @NotNull List<@NotNull ClassTransformer> transformers) {

        try {
            final var jigsawConfig = config.get(ClockworkConfig.SPEC.JIGSAW_STRATEGY);
            final var strategy = featureProviders.newFeature(JigsawStrategy.class, this, jigsawConfig);
            return strategy.buildModuleLayers(plugins, config.get(ClockworkConfig.SPEC.LIB_MODULE_PATH), transformers, parent);
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
        final var target = controller.defineTargetType(plugin, parentType, descriptor, targetClass);

        // Then add it to the core and plugin.
        controller.addLoadedTargetType(target);

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
        final var component = controller.defineComponentType(plugin, descriptor, targetType, compCasted);

        // Then add it to the core and plugin.
        controller.addLoadedComponentType(component);

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
        core.getPhase().require(Phase.PROCESSED);

        // Get the core target.
        final var coreTarget = core.getTargetTypeOrThrow(ClockworkCore.class);

        // Build the component container and set it.
        final var container = new SimpleComponentContainer(coreTarget, core);
        controller.setCoreContainer(container);

        // Init the components and update the state of the core.
        container.initComponents();

        // Notify extensions about phase change.
        controller.setState(Phase.INITIALISED);
        extensions.apply(this, LoaderExtension::onCoreInitialised);

    }

    private void addProblem(@NotNull PluginLoadingProblem problem) {
        (problem.isFatal() ? fatalProblems : skippedProblems).add(problem);
    }

    @Override
    public ComponentContainer getComponentContainer() {
        return extensionContainer;
    }

    public <T extends LoaderExtension> @NotNull T getExtension(@NotNull Class<T> extensionClass) {
        final var ext = extensions.getTargetType().getComponentTypeOrThrow(extensionClass).get(this);
        if (ext == null) throw ClockworkException.generic("Extension from class [] missing", extensionClass.getSimpleName());
        return ext;
    }

    public ClockworkCore getCore() {
        return core;
    }

    public Config getConfig() {
        return config;
    }

    public @NotNull Lookup getReflectiveAccess(@NotNull LoadedPlugin plugin,
                                               @NotNull Class<?> targetClass,
                                               @NotNull AccessLevel requiredLevel) {

        Objects.requireNonNull(targetClass);
        Objects.requireNonNull(requiredLevel);

        final var targetModule = targetClass.getModule();
        if (plugin.getClockworkCore() != core) throw new IllegalArgumentException();
        if (targetModule != plugin.getMainModule())
            throw ClockworkException.generic("Module [] is not accessible from plugin []", targetModule, plugin);

        final var lookup = fetchLookup(plugin, targetClass);

        if (requiredLevel == AccessLevel.FULL && !lookup.hasFullPrivilegeAccess())
            throw ClockworkException.generic("This operation requires full reflective access on plugin [], " +
                    "but its module does not provide such access.", plugin);

        if (requiredLevel == AccessLevel.PRIVATE && (lookup.lookupModes() & Lookup.PRIVATE) == 0)
            throw ClockworkException.generic("This operation requires private reflective access on plugin [], " +
                    "but its module does not provide such access.", plugin);

        return lookup;
    }

    private Lookup fetchLookup(LoadedPlugin plugin, Class<?> targetClass) {

        // First, attempt to get the lookup directly from the plugin's main component.
        if (core.getPhase() == ClockworkCore.Phase.INITIALISED) {
            final var providedLookup = controller.getReflectiveAccess(plugin);
            if (providedLookup != null) {
                if (providedLookup.hasFullPrivilegeAccess()) {
                    try {
                        return MethodHandles.privateLookupIn(targetClass, providedLookup);
                    } catch (Throwable t) {
                        // If something went wrong, ignore and try the next best option.
                    }
                }
                return providedLookup.in(targetClass);
            }
        }

        // If the main component did not provide one, try to use the root lookup to obtain it.
        if (plugin.getMainModule().isOpen(targetClass.getPackageName(), INTERNAL_LOOKUP.lookupClass().getModule())) {
            INTERNAL_LOOKUP.lookupClass().getModule().addReads(plugin.getMainModule());
            try {
                return MethodHandles.privateLookupIn(targetClass, INTERNAL_LOOKUP);
            } catch (Throwable t) {
                // If something went wrong, ignore and try the next best option.
            }
        }

        // If none of these options worked, fallback to public lookup.
        return INTERNAL_LOOKUP.in(targetClass);

    }

    public <C extends Component<T>, T extends ComponentTarget>
    void setComponentFactory(@NotNull RegisteredComponentType<C, T> componentType, @NotNull ComponentFactory<T, C> factory) {
        core.getPhase().require(Phase.POPULATED);
        Objects.requireNonNull(componentType);
        Objects.requireNonNull(factory);
        controller.setComponentFactory(componentType, factory);
    }

    public enum AccessLevel {
        PUBLIC, PRIVATE, FULL
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
