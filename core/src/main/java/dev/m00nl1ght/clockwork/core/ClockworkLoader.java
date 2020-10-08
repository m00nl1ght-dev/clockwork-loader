package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.classloading.ModuleManager;
import dev.m00nl1ght.clockwork.container.ImmutableComponentContainer;
import dev.m00nl1ght.clockwork.core.ClockworkCore.State;
import dev.m00nl1ght.clockwork.core.plugin.CWLPlugin;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.descriptor.ComponentDescriptor;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.descriptor.TargetDescriptor;
import dev.m00nl1ght.clockwork.fnder.ModuleLayerPluginFinder;
import dev.m00nl1ght.clockwork.fnder.ModulePathPluginFinder;
import dev.m00nl1ght.clockwork.fnder.NestedPluginFinder;
import dev.m00nl1ght.clockwork.fnder.PluginFinderType;
import dev.m00nl1ght.clockwork.reader.ManifestPluginReader;
import dev.m00nl1ght.clockwork.reader.PluginReaderType;
import dev.m00nl1ght.clockwork.util.AbstractTopologicalSorter;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.Registry;
import dev.m00nl1ght.clockwork.verifier.PluginVerifierType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Lookup INTERNAL_LOOKUP = MethodHandles.lookup();
    private static final int TARGET_JAVA_VERSION = 14;

    static {
        final var version = Runtime.version();
        if (version.feature() != TARGET_JAVA_VERSION)
            LOGGER.warn("The current Java version {} is not fully supported. CWL was developed for Java {}. " +
                    "Using any other version can cause instability and crashes.", version, TARGET_JAVA_VERSION);
        if (ClockworkLoader.class.getModule().getName() == null)
            throw FormatUtil.rtExc("Core module was not loaded correctly (the module is unnamed)");
    }

    public static ClockworkLoader build(ClockworkConfig config) {
        return new ClockworkLoader(null, Arguments.notNull(config, "config"));
    }

    public static ClockworkLoader build(ClockworkCore parent, ClockworkConfig config) {
        Arguments.notNull(parent, "parent").getState().requireOrAfter(State.INITIALISED);
        return new ClockworkLoader(parent, Arguments.notNull(config, "config"));
    }

    public static ClockworkLoader buildBootLayerDefault() {
        final var configBuilder = ClockworkConfig.builder();
        configBuilder.addPluginReader(ManifestPluginReader.newConfig("manifest"));
        configBuilder.addPluginFinder(ModuleLayerPluginFinder.newConfig("boot", true));
        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("clockwork"));
        return build(configBuilder.build());
    }

    private final ClockworkCore parent;
    private final ClockworkConfig config;
    private ClockworkCore core;

    private final Registry<PluginReaderType> readerTypeRegistry = new Registry<>(PluginReaderType.class);
    private final Registry<PluginFinderType> finderTypeRegistry = new Registry<>(PluginFinderType.class);
    private final Registry<PluginVerifierType> verifierTypeRegistry = new Registry<>(PluginVerifierType.class);
    private final Registry<PluginProcessor> processorRegistry = new Registry<>(PluginProcessor.class);

    private final List<PluginLoadingProblem> fatalProblems = new ArrayList<>();
    private final List<PluginLoadingProblem> skippedProblems = new ArrayList<>();

    private ClockworkLoader(ClockworkCore parent, ClockworkConfig config) {
        this.parent = parent;
        this.config = config;
        registerDefaults();
    }

    private void registerDefaults() {
        ManifestPluginReader.registerTo(readerTypeRegistry);
        ModuleLayerPluginFinder.registerTo(finderTypeRegistry);
        ModulePathPluginFinder.registerTo(finderTypeRegistry);
        NestedPluginFinder.registerTo(finderTypeRegistry);
    }

    public synchronized void collectExtensionsFromParent() {
        if (parent == null) return;
        final var cwlPluginComp = parent.getComponentType(CWLPlugin.class, ClockworkCore.class).orElseThrow();
        final var cwlPlugin = Objects.requireNonNull(cwlPluginComp.get(parent));
        cwlPlugin.getCollectExtensionsEventType().post(parent, new CollectClockworkExtensionsEvent(this));
    }

    public List<PluginLoadingProblem> getFatalProblems() {
        return Collections.unmodifiableList(fatalProblems);
    }

    public List<PluginLoadingProblem> getSkippedProblems() {
        return Collections.unmodifiableList(skippedProblems);
    }

    public ClockworkCore loadAndInit() {
        this.load();
        this.init();
        return core;
    }

    public synchronized ClockworkCore load() {

        if (core != null) throw new IllegalStateException("Already loaded");

        final var pluginReferences = new LinkedList<PluginReference>();
        final var versionSorter = Comparator.comparing(PluginReference::getVersion).reversed();
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
        final var loadingContext = LoadingContext.of(config, this);
        for (final var config : config.getFinders()) {
            if (config.isWildcard()) {
                final var finder = loadingContext.getFinder(config.getName());
                for (final var located : finder.findAll(loadingContext)) {
                    wantedPlugins.computeIfAbsent(located.getId(), k -> DependencyDescriptor.buildAnyVersion(located.getId()));
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
            final var located = new LinkedList<PluginReference>();
            for (var finder : loadingContext.getFinders()) {
                for (var ref : finder.find(loadingContext, wanted)) {
                    located.add(ref);
                    LOGGER.debug("Located plugin [" + ref + "] using finder [" + finder + "].");
                }
            }

            // If anything was found, add it to the sorters.
            if (located.isEmpty()) {
                addProblem(PluginLoadingProblem.pluginNotFound(wanted));
            } else {
                located.sort(versionSorter);
                final var ref = located.get(0);
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
            LOGGER.error("The following fatal problems occurred during dependency resolution:");
            for (var p : fatalProblems) LOGGER.error(p.format());
            throw PluginLoadingException.fatalLoadingProblems(fatalProblems);
        }

        // If there were any other problems, just print them.
        if (!skippedProblems.isEmpty()) {
            LOGGER.info("The following optional components have been skipped, because their dependencies are not present:");
            for (var p : skippedProblems) LOGGER.info(p.format());
        }

        // Create the new ModuleLayer and the ClockworkCore instance.
        final var parentLayer = parent == null ? ModuleLayer.boot() : parent.getModuleLayer();
        final var moduleManager = new ModuleManager(loadingContext, pluginReferences, parentLayer);
        core = new ClockworkCore(moduleManager);

        // First add the plugins inherited from the parent.
        if (parent != null) {
            for (final var inherited : parent.getLoadedPlugins()) {
                final var plugin = new LoadedPlugin(inherited.getDescriptor(), core, inherited.getMainModule());
                core.addLoadedPlugin(plugin);
            }
        }

        // Then add the new ones that were located using the config.
        for (final var pluginReference : pluginReferences) {
            final var mainModule = moduleManager.getModuleLayer()
                    .findModule(pluginReference.getMainModule().descriptor().name()).orElseThrow();
            final var plugin = new LoadedPlugin(pluginReference.getDescriptor(), core, mainModule);
            moduleManager.patchModule(mainModule);
            core.addLoadedPlugin(plugin);
        }

        // Next, prepare and add all targets provided by the plugins.
        for (final var targetDescriptor : targetDescriptors) {

            // Get the plugin that is providing this target.
            final var plugin = core.getLoadedPlugin(targetDescriptor.getPluginId()).orElseThrow();

            // If the parent has it, get the target class from there.
            if (parent != null) {
                final var inherited = parent.getTargetType(targetDescriptor.getId());
                if (inherited.isPresent()) {
                    buildTarget(plugin, targetDescriptor, inherited.get().getTargetClass());
                    continue;
                }
            }

            // Otherwise, get the target class from the ModuleManager, then verify and cast it.
            final var targetClass = moduleManager.loadClassForPlugin(targetDescriptor.getTargetClass(), plugin);
            if (!ComponentTarget.class.isAssignableFrom(targetClass))
                throw PluginLoadingException.invalidTargetClass(targetDescriptor, targetClass);
            @SuppressWarnings("unchecked")
            final var targetCasted = (Class<? extends ComponentTarget>) targetClass;
            buildTarget(plugin, targetDescriptor, targetCasted);

        }

        // Now, prepare and add all components provided by the plugins.
        for (final var componentDescriptor : componentDescriptors) {

            // Get the plugin that is providing this component, and the target it is for.
            final var plugin = core.getLoadedPlugin(componentDescriptor.getPluginId()).orElseThrow();
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

            // Otherwise, get the component class from the ModuleManager.
            final var componentClass = moduleManager.loadClassForPlugin(componentDescriptor.getComponentClass(), plugin);
            buildComponent(plugin, componentDescriptor, target.get(), componentClass);

        }

        // Add internal components of each target type.
        for (final var targetType : core.getLoadedTargetTypes()) {
            final var classes = targetType.getDescriptor().getInternalComponents();
            for (final var className : classes) {
                final var compClass = moduleManager.loadClassOrNull(className);
                if (compClass == null)
                    throw PluginLoadingException.pluginClassNotFound(className, targetType.getPlugin().getDescriptor());
                buildInternalComponent(targetType, compClass);
            }
        }

        core.setState(State.PROCESSING);

        // Notify all registered plugin processors.
        for (final var entry : processorRegistry.getRegistered().entrySet()) {
            try {
                entry.getValue().onLoadingStart(core, parent);
            } catch (Throwable t) {
                throw PluginLoadingException.inProcessor(entry.getKey(), t);
            }
        }

        // Apply the plugin processors defined to each plugin respectively.
        for (final var plugin : core.getLoadedPlugins()) {

            // Get the processors, and skip the plugin if there are none.
            final var processors = plugin.getDescriptor().getProcessors();
            if (processors.isEmpty()) continue;

            // Now apply the processors.
            final var reflectiveAccess = new PluginProcessorContext(plugin, INTERNAL_LOOKUP);
            for (var name : processors) {
                final var optional = name.startsWith("?");
                if (optional) name = name.substring(1);
                final var processor = processorRegistry.get(name);
                if (processor == null) {
                    if (!optional) throw PluginLoadingException.missingProcessor(plugin.getId(), name);
                } else {
                    try {
                        processor.process(reflectiveAccess);
                    } catch (Throwable t) {
                        throw PluginLoadingException.inProcessor(plugin, name, t);
                    }
                }
            }

        }

        // Initialise the target types.
        core.getLoadedTargetTypes().forEach(TargetType::init);

        // The core is now ready for use.
        core.setState(State.POPULATED);
        return core;
    }

    private <T extends ComponentTarget> void
    buildTarget(LoadedPlugin plugin, TargetDescriptor descriptor, Class<T> targetClass) {

        // First, fetch the parent target if there is any, and verify it.
        TargetType<? super T> parentType = null;
        if (descriptor.getParent() != null) {
            final var found = plugin.getClockworkCore().getTargetType(descriptor.getParent()).orElseThrow();
            if (found.getTargetClass().isAssignableFrom(targetClass)) {
                @SuppressWarnings("unchecked")
                final var casted = (TargetType<? super T>) found;
                parentType = casted;
            } else {
                throw PluginLoadingException.invalidParentForTarget(descriptor, found);
            }
        }

        // Construct the new TargetType.
        final var target = new RegisteredTargetType<>(plugin, parentType, descriptor, targetClass);

        // Then add it to the core and plugin.
        plugin.getClockworkCore().addLoadedTargetType(target);
        plugin.addLoadedTargetType(target);

    }

    private <C, T extends ComponentTarget> void
    buildComponent(LoadedPlugin plugin, ComponentDescriptor descriptor, RegisteredTargetType<T> targetType, Class<C> componentClass) {

        // First, fetch the parent target if there is any, and verify it.
        ComponentType<? super C, ? super T> parentType = null;
        if (descriptor.getParent() != null) {
            final var found = plugin.getClockworkCore().getComponentType(descriptor.getParent()).orElseThrow();
            if (found.getComponentClass().isAssignableFrom(componentClass)) {
                @SuppressWarnings("unchecked")
                final var casted = (ComponentType<? super C, ? super T>) found;
                parentType = casted;
            } else {
                throw PluginLoadingException.invalidParentForComponent(descriptor, found);
            }
        }

        // Construct the new ComponentType.
        final var component = new RegisteredComponentType<>(plugin, parentType, descriptor, componentClass, targetType);

        // Then add it to the core and plugin.
        plugin.getClockworkCore().addLoadedComponentType(component);
        plugin.addLoadedComponentType(component);

    }

    private <C, T extends ComponentTarget> void
    buildInternalComponent(RegisteredTargetType<T> targetType, Class<C> compClass) {

        final var scOpt = targetType.getComponentTypes().stream()
                .filter(c -> c.componentClass.isAssignableFrom(compClass))
                .findFirst();

        @SuppressWarnings("unchecked")
        final var casted = scOpt.isEmpty() ? null : (ComponentType<? super C, ? super T>) scOpt.get();
        new ComponentType<>(casted, compClass, targetType);

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
        core.getState().require(State.POPULATED);

        // Get the core target.
        final var coreTarget = core.getTargetType(ClockworkCore.class);
        if (coreTarget.isEmpty()) throw PluginLoadingException.coreTargetMissing(ClockworkCore.CORE_TARGET_ID);

        // Build the component container and set it.
        final var container = new ImmutableComponentContainer<>(coreTarget.get(), core);
        core.setCoreContainer(container);

        // Init the components and update the state of the core.
        container.initComponents();
        core.setState(State.INITIALISED);

        // Notify all registered plugin processors.
        for (final var entry : processorRegistry.getRegistered().entrySet()) {
            try {
                entry.getValue().onLoadingComplete(core);
            } catch (Throwable t) {
                throw PluginLoadingException.inProcessor(entry.getKey(), t);
            }
        }

    }

    public Registry<PluginReaderType> getReaderTypeRegistry() {
        return readerTypeRegistry;
    }

    public Registry<PluginFinderType> getFinderTypeRegistry() {
        return finderTypeRegistry;
    }

    public Registry<PluginVerifierType> getVerifierTypeRegistry() {
        return verifierTypeRegistry;
    }

    public Registry<PluginProcessor> getProcessorRegistry() {
        return processorRegistry;
    }

    private void addProblem(PluginLoadingProblem problem) {
        (problem.isFatal() ? fatalProblems : skippedProblems).add(problem);
    }

    static Lookup getInternalLookup() {
        return INTERNAL_LOOKUP;
    }

    private class ComponentSorter extends AbstractTopologicalSorter<ComponentDescriptor, DependencyDescriptor> {

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

    private class TargetSorter extends AbstractTopologicalSorter<TargetDescriptor, String> {

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
