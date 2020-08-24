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

        if (parent != null) {
            for (final var c : parent.getLoadedComponentTypes()) componentSorter.add(c.getDescriptor());
            for (final var t : parent.getLoadedTargetTypes()) targetSorter.add(t.getDescriptor());
        }

        for (final var wanted : config.getWantedComponents()) {

            if (parent != null) {
                final var inherited = parent.getComponentType(wanted.getTarget());
                if (inherited.isPresent()) {
                    if (!wanted.acceptsVersion(inherited.get().getDescriptor().getVersion()))
                        addProblem(PluginLoadingProblem.inheritedVersionClash(wanted, inherited.get().getDescriptor()));
                    continue;
                }
            }

            final var located = new LinkedList<PluginReference>();
            for (var locator : config.getPluginLocators()) {
                for (var ref : locator.find(wanted)) {
                    located.add(ref);
                    if (ref.getLocator() != locator)
                        addProblem(PluginLoadingProblem.locatorMismatch(ref, locator));
                }
            }

            if (located.isEmpty()) {
                addProblem(PluginLoadingProblem.pluginNotFound(wanted));
            } else {
                located.sort(versionSorter);
                final var ref = located.get(0);
                pluginReferences.addLast(ref);
                ref.getComponentDefinitions().forEach(componentSorter::add);
                ref.getTargetDefinitions().forEach(targetSorter::add);
                LOGGER.debug("Located plugin [" + ref + "] using locator [" + ref.getLocator().getName() + "].");
            }

        }

        final var componentDescriptors = new LinkedList<ComponentDescriptor>();
        final var targetDescriptors = new LinkedList<TargetDescriptor>();

        componentSorter.sort(componentDescriptors);
        targetSorter.sort(targetDescriptors);

        if (!fatalProblems.isEmpty()) {
            LOGGER.error("The following fatal problems occurred during dependency resolution:");
            for (var p : fatalProblems) LOGGER.error(p.format());
            throw PluginLoadingException.fatalLoadingProblems(fatalProblems);
        }

        if (!skippedProblems.isEmpty()) {
            LOGGER.info("The following optional components have been skipped, because their dependencies are not present:");
            for (var p : skippedProblems) LOGGER.info(p.format());
        }

        final var parentLayer = parent == null ? ModuleLayer.boot() : parent.getModuleLayer();
        final var moduleManager = new ModuleManager(pluginReferences, parentLayer);
        final var core = new ClockworkCore(moduleManager);

        for (final var pluginReference : pluginReferences) {
            final var mainModule = moduleManager.mainModuleFor(pluginReference);
            final var plugin = new LoadedPlugin(pluginReference.getDescriptor(), core, mainModule);
            moduleManager.bindModule(plugin, mainModule.getName());
            core.addLoadedPlugin(plugin);
        }

        for (final var targetDescriptor : targetDescriptors) {
            final var plugin = core.getLoadedPlugin(targetDescriptor.getPlugin().getId()).orElseThrow();
            final var targetClass = moduleManager.loadClassForPlugin(targetDescriptor.getTargetClass(), plugin);
            if (!ComponentTarget.class.isAssignableFrom(targetClass)) throw PluginLoadingException.invalidTargetClass(targetDescriptor, targetClass);
            @SuppressWarnings("unchecked") final var targetCasted = (Class<? extends ComponentTarget>) targetClass;
            final var target = TargetType.create(plugin, targetDescriptor, targetCasted);
            core.addLoadedTargetType(target);
        }

        for (final var componentDescriptor : componentDescriptors) {
            final var plugin = core.getLoadedPlugin(componentDescriptor.getPlugin().getId()).orElseThrow();
            final var compClass = moduleManager.loadClassForPlugin(componentDescriptor.getComponentClass(), plugin);
            final var target = core.getTargetType(componentDescriptor.getTargetId());
            if (target.isEmpty()) throw PluginLoadingException.componentMissingTarget(componentDescriptor);
            final var component = target.get().getPrimer().register(componentDescriptor, plugin, compClass);
            core.addLoadedComponentType(component);
        }

        for (var targetType : core.getLoadedTargetTypes()) targetType.getPrimer().init();
        // TODO plugin processors
        // this.state = State.LOCATED;

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
