package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.classloading.ModuleManager;
import dev.m00nl1ght.clockwork.processor.PluginProcessorManager;
import dev.m00nl1ght.clockwork.util.AbstractTopologicalSorter;
import dev.m00nl1ght.clockwork.util.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * The entry point of the plugin loading framework.
 *
 * From application code, call {@link ClockworkLoader#load}
 * to get a ClockworkCore instance.
 */
public final class ClockworkLoader {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Finds plugins based on the given {@link ClockworkConfig} and resolves all dependencies.
     *
     * @param config the {@link ClockworkConfig} defining how plugins will be located
     * @return a new {@link ClockworkCore} that can be used to load the plugins that have been located
     * @throws PluginLoadingException if there were any fatal dependency resolution problems
     */
    public static ClockworkCore load(ClockworkConfig config) {
        return new ClockworkLoader(null, Preconditions.notNull(config, "config")).load();
    }

    public static ClockworkCore load(ClockworkCore parent, ClockworkConfig config) {
        final var state = Preconditions.notNull(parent, "parent").getState();
        if (state == ClockworkCore.State.CONSTRUCTED) throw new IllegalArgumentException();
        return new ClockworkLoader(parent, Preconditions.notNull(config, "config")).load();
    }

    private final ClockworkCore parent;
    private final ClockworkConfig config;

    private final PluginProcessorManager pluginProcessors = new PluginProcessorManager(MethodHandles.lookup());

    private final List<PluginLoadingProblem> fatalProblems = new ArrayList<>();
    private final List<PluginLoadingProblem> skippedProblems = new ArrayList<>();

    private ClockworkLoader(ClockworkCore parent, ClockworkConfig config) {
        this.parent = parent;
        this.config = config;
    }

    private ClockworkCore load() {

        final var pluginReferences = new LinkedList<PluginReference>();
        final var versionSorter = Comparator.comparing(PluginReference::getVersion).reversed();
        final var componentSorter = new ComponentSorter();
        final var targetSorter = new TargetSorter();

        // If there is a parent core, add it's components and targets to the sorters first.
        if (parent != null) {
            for (final var c : parent.getLoadedComponentTypes()) componentSorter.add(c.getDescriptor());
            for (final var t : parent.getLoadedTargetTypes()) targetSorter.add(t.getDescriptor());
        }

        // Now try to find all the plugins that are wanted by the config.
        for (final var wanted : config.getWantedPlugins()) {

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
                    final var targetClass = inherited.get().getTargetClass();
                    final var target = TargetType.create(plugin, targetDescriptor, targetClass);
                    core.addLoadedTargetType(target);
                    continue;
                }
            }

            // Otherwise, get the target class from the ModuleManager, then verify and cast it.
            final var targetClass = moduleManager.loadClassForPlugin(targetDescriptor.getTargetClass(), plugin);
            if (!ComponentTarget.class.isAssignableFrom(targetClass))
                throw PluginLoadingException.invalidTargetClass(targetDescriptor, targetClass);
            @SuppressWarnings("unchecked") final var targetCasted = (Class<? extends ComponentTarget>) targetClass;

            // Finally, construct the new TargetType and add it to the core.
            final var target = TargetType.create(plugin, targetDescriptor, targetCasted);
            core.addLoadedTargetType(target);

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
                    final var componentClass = inherited.get().getComponentClass();
                    final var component = target.get().addComponent(componentDescriptor, plugin, componentClass);
                    core.addLoadedComponentType(component);
                    continue;
                }
            }

            // Otherwise, get the component class from the ModuleManager.
            final var compClass = moduleManager.loadClassForPlugin(componentDescriptor.getComponentClass(), plugin);

            // Finally, construct the new ComponentType and add it to the core.
            final var component = target.get().addComponent(componentDescriptor, plugin, compClass);
            core.addLoadedComponentType(component);

        }

        // Lock the internal registries of the core.
        core.lock();

        // Apply all plugin processors defined to each plugin respectively.
        for (final var pluginReference : pluginReferences) {

            // Get the processors, and skip the plugin if there are none.
            final var processors = pluginReference.getProcessors();
            if (processors.isEmpty()) continue;

            // Get the corresponding LoadedPlugin instance and apply the processors.
            final var plugin = core.getLoadedPlugin(pluginReference.getId()).orElseThrow();
            pluginProcessors.apply(plugin, pluginReference.getProcessors());

            // Apply the processors to all TargetTypes provided by the plugin next.
            for (final var targetDescriptor : pluginReference.getTargetDescriptors()) {
                final var target = core.getTargetType(targetDescriptor.getId()).orElseThrow();
                pluginProcessors.apply(target, pluginReference.getProcessors());
            }

            // Finally, apply the processors to all ComponentTypes provided by the plugin as well.
            for (final var componentDescriptor : pluginReference.getComponentDescriptors()) {
                final var component = core.getComponentType(componentDescriptor.getId()).orElseThrow();
                pluginProcessors.apply(component, pluginReference.getProcessors());
            }

        }

        // The core is now ready for use.
        return core;
    }

    private void addProblem(PluginLoadingProblem problem) {
        (problem.isFatal() ? fatalProblems : skippedProblems).add(problem);
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

    public class TargetSorter extends AbstractTopologicalSorter<TargetDescriptor, String> {

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
