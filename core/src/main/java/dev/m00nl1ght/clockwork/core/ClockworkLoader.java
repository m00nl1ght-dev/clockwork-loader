package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.classloading.ModuleManager;
import dev.m00nl1ght.clockwork.core.ClockworkCore.State;
import dev.m00nl1ght.clockwork.core.plugin.CWLPlugin;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.descriptor.ComponentDescriptor;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.descriptor.TargetDescriptor;
import dev.m00nl1ght.clockwork.locator.BootLayerLocator;
import dev.m00nl1ght.clockwork.processor.PluginProcessor;
import dev.m00nl1ght.clockwork.processor.ReflectiveAccess;
import dev.m00nl1ght.clockwork.util.AbstractTopologicalSorter;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.Preconditions;
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
 * to get a ClockworkCore instance.
 */
public final class ClockworkLoader {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Lookup INTERNAL_REFLECTIVE_ACCESS = MethodHandles.lookup();

    /**
     * Finds plugins based on the given {@link ClockworkConfig} and resolves all dependencies.
     *
     * @param config the {@link ClockworkConfig} defining how plugins will be located
     * @return a new {@link ClockworkCore} that can be used to load the plugins that have been located
     * @throws PluginLoadingException if there were any fatal dependency resolution problems
     */
    public static ClockworkLoader build(ClockworkConfig config) {
        return new ClockworkLoader(null, Preconditions.notNull(config, "config"));
    }

    public static ClockworkLoader build(ClockworkCore parent, ClockworkConfig config) {
        Preconditions.notNull(parent, "parent").getState().requireOrAfter(State.POPULATED);
        return new ClockworkLoader(parent, Preconditions.notNull(config, "config"));
    }

    public static ClockworkLoader buildBootLayerDefault() {
        final var configBuilder = ClockworkConfig.builder();
        configBuilder.addPluginLocator(new BootLayerLocator(), true);
        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("clockwork"));
        return build(configBuilder.build());
    }

    private final ClockworkCore parent;
    private final ClockworkConfig config;

    private final Map<String, PluginProcessor> registeredProcessors = new HashMap<>();

    private final List<PluginLoadingProblem> fatalProblems = new ArrayList<>();
    private final List<PluginLoadingProblem> skippedProblems = new ArrayList<>();

    private ClockworkLoader(ClockworkCore parent, ClockworkConfig config) {
        this.parent = parent;
        this.config = config;
    }

    public void registerPluginProcessor(String id, PluginProcessor pluginProcessor) {
        final var existing = registeredProcessors.putIfAbsent(id, pluginProcessor);
        if (existing != null) throw FormatUtil.illArgExc("Plugin processor with id [] is already present", id);
    }

    public void collectExtensionsFromParent() {
        if (parent == null) return;
        parent.getState().requireOrAfter(State.INITIALISED);
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

    public ClockworkCore load() {

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

        // Also add the plugins found by wildcard locators.
        for (final var wildcardLocator : config.getWantedWildcard()) {
            for (final var located : wildcardLocator.findAll()) {
                wantedPlugins.computeIfAbsent(located.getId(), k -> DependencyDescriptor.buildAnyVersion(located.getId()));
            }
        }

        // Now try to find all those plugins.
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

            // Otherwise, try to find it with the PluginLocators from the config.
            final var located = new LinkedList<PluginReference>();
            for (var locator : config.getPluginLocators()) {
                for (var ref : locator.find(wanted)) {
                    located.add(ref);
                    if (ref.getLocator() != locator)
                        addProblem(PluginLoadingProblem.locatorMismatch(ref, locator));
                }
            }

            // If anything was found, add it to the sorters.
            if (located.isEmpty()) {
                addProblem(PluginLoadingProblem.pluginNotFound(wanted));
            } else {
                located.sort(versionSorter);
                final var ref = located.get(0);
                pluginReferences.addLast(ref);
                ref.getComponentDescriptors().forEach(componentSorter::add);
                ref.getTargetDescriptors().forEach(targetSorter::add);
                LOGGER.debug("Located plugin [" + ref + "] using locator [" + ref.getLocator().getName() + "].");
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
        final var moduleManager = new ModuleManager(pluginReferences, parentLayer);
        final var core = new ClockworkCore(moduleManager);

        // First add the plugins inherited from the parent.
        if (parent != null) {
            for (final var inherited : parent.getLoadedPlugins()) {
                final var plugin = new LoadedPlugin(inherited.getDescriptor(), core, inherited.getMainModule());
                core.addLoadedPlugin(plugin);
            }
        }

        // Then add the new ones that were located using the config.
        for (final var pluginReference : pluginReferences) {
            final var mainModule = moduleManager.mainModuleFor(pluginReference);
            final var plugin = new LoadedPlugin(pluginReference.getDescriptor(), core, mainModule);
            moduleManager.bindModule(plugin, mainModule.getName());
            core.addLoadedPlugin(plugin);
        }

        // Next, prepare and add all targets provided by the plugins.
        for (final var targetDescriptor : targetDescriptors) {

            // Get the plugin that is providing this target.
            final var plugin = core.getLoadedPlugin(targetDescriptor.getPlugin().getId()).orElseThrow();

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
            @SuppressWarnings("unchecked") final var targetCasted = (Class<? extends ComponentTarget>) targetClass;
            buildTarget(plugin, targetDescriptor, targetCasted);

        }

        // Also, prepare and add all components provided by the plugins.
        for (final var componentDescriptor : componentDescriptors) {

            // Get the plugin that is providing this component, and the target it is for.
            final var plugin = core.getLoadedPlugin(componentDescriptor.getPlugin().getId()).orElseThrow();
            final var target = core.getTargetType(componentDescriptor.getTargetId());
            if (target.isEmpty()) throw PluginLoadingException.componentMissingTarget(componentDescriptor);

            // If the parent has it, get the component class from there.
            if (parent != null) {
                final var inherited = parent.getComponentType(componentDescriptor.getId());
                if (inherited.isPresent()) {
                    buildComponent(plugin, componentDescriptor, target.get(), inherited.get().getComponentClass());
                    continue;
                }
            }

            // Otherwise, get the component class from the ModuleManager.
            final var componentClass = moduleManager.loadClassForPlugin(componentDescriptor.getComponentClass(), plugin);
            buildComponent(plugin, componentDescriptor, target.get(), componentClass);

        }

        // Init and lock the target types.
        for (var targetType : core.getLoadedTargetTypes()) targetType.init();
        core.setState(State.PROCESSING);

        // Apply all plugin processors defined to each plugin respectively.
        for (final var pluginReference : pluginReferences) {

            // Get the processors, and skip the plugin if there are none.
            final var processors = pluginReference.getProcessors();
            if (processors.isEmpty()) continue;

            // Get the corresponding LoadedPlugin instance and apply the processors.
            final var plugin = core.getLoadedPlugin(pluginReference.getId()).orElseThrow();
            final var reflectiveAccess = new ReflectiveAccess(plugin, INTERNAL_REFLECTIVE_ACCESS);
            for (var name : processors) {
                final var processor = registeredProcessors.get(name);
                if (processor == null) throw PluginLoadingException.missingProcessor(plugin.getId(), name);
                try {
                    processor.process(plugin, reflectiveAccess);
                } catch (Throwable t) {
                    throw PluginLoadingException.inProcessor("plugin", plugin.getId(), name, t);
                }
            }

        }

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
                @SuppressWarnings("unchecked") final var casted = (TargetType<? super T>) found;
                parentType = casted;
            } else {
                throw PluginLoadingException.invalidParentForTarget(descriptor, found);
            }
        }

        // Assert that there are no inconsistencies in the target hierarchy.
        Class<?> sctCurrent = targetClass;
        final var sctExpected = parentType == null ? Object.class : parentType.getTargetClass();
        while ((sctCurrent = sctCurrent.getSuperclass()) != null) {
            if (sctCurrent == sctExpected) break;
            final var found = plugin.getClockworkCore().getTargetTypeUncasted(sctCurrent);
            if (found.isPresent()) {
                throw PluginLoadingException.illegalTargetSubclass(descriptor, targetClass, found.get());
            }
        }

        // Construct the new TargetType.
        final var target = new TargetType<>(plugin, parentType, descriptor, targetClass);

        // Then add it to the core and plugin.
        plugin.getClockworkCore().addLoadedTargetType(target);
        plugin.addLoadedTargetType(target);

    }

    private <C, T extends ComponentTarget> void
    buildComponent(LoadedPlugin plugin, ComponentDescriptor descriptor, TargetType<T> targetType, Class<C> componentClass) {

        // Construct the new ComponentType.
        final var component = new ComponentType<>(plugin, descriptor, componentClass, targetType);

        // Then add it to the core, plugin and target.
        plugin.getClockworkCore().addLoadedComponentType(component);
        plugin.addLoadedComponentType(component);
        targetType.addComponent(component);

    }

    private void addProblem(PluginLoadingProblem problem) {
        (problem.isFatal() ? fatalProblems : skippedProblems).add(problem);
    }

    static Lookup getInternalReflectiveAccess() {
        return INTERNAL_REFLECTIVE_ACCESS;
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
            addProblem(PluginLoadingProblem.duplicateIdFound(node.getPlugin(), node, present));
        }

        @Override
        public void onCycleFound(ComponentDescriptor tail) {
            addProblem(PluginLoadingProblem.depCycleFound(tail.getPlugin(), tail));
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
            addProblem(PluginLoadingProblem.duplicateIdFound(node.getPlugin(), node, present));
        }

        @Override
        public void onCycleFound(TargetDescriptor tail) {
            addProblem(PluginLoadingProblem.depCycleFound(tail.getPlugin(), tail));
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
