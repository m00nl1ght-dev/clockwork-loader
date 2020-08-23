package dev.m00nl1ght.clockwork.resolver;

import dev.m00nl1ght.clockwork.core.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class DependencyResolver {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ClockworkConfig config;

    private final TopologicalSorter<ComponentDescriptor, DependencyDescriptor> compSorter = new TopologicalSorter<>(new CompSortFuncs());
    private final TopologicalSorter<TargetDescriptor, String> targetSorter = new TopologicalSorter<>(new TargetSortFuncs());
    private final LinkedList<PluginReference> pluginReferences = new LinkedList<>();
    private final LinkedList<ComponentDescriptor> componentDescriptors = new LinkedList<>();
    private final LinkedList<TargetDescriptor> targetDescriptors = new LinkedList<>();
    private final List<PluginLoadingProblem> fatalProblems = new ArrayList<>();
    private final List<PluginLoadingProblem> skippedProblems = new ArrayList<>();

    public DependencyResolver(ClockworkConfig config) {
        this.config = config;
    }

    private void addDefinition(PluginReference def) {
        pluginReferences.add(def);
        def.getTargetDefinitions().forEach(this::addDefinition);
        def.getComponentDefinitions().forEach(this::addDefinition);
    }

    private void addDefinition(ComponentDescriptor def) {
        final var present = compSorter.add(def);
        if (present != null) addProblem(PluginLoadingProblem.duplicateIdFound(def.getPlugin(), def, present));
    }

    private void addDefinition(TargetDescriptor def) {
        final var present = targetSorter.add(def);
        if (present != null) addProblem(PluginLoadingProblem.duplicateIdFound(def.getPlugin(), def, present));
    }

    public void resolveAndSort() {
        final Comparator<PluginReference> sorter = Comparator.comparing(PluginReference::getVersion).reversed();

        for (final var descriptor : config.getComponentDescriptors()) {
            final var found = new ArrayList<PluginReference>();
            for (var locator : config.getPluginLocators()) {
                for (var def : locator.find(descriptor)) {
                    found.add(def);
                    if (def.getLocator() != locator)
                        addProblem(PluginLoadingProblem.locatorMismatch(def, locator));
                }
            }

            if (found.isEmpty()) {
                addProblem(PluginLoadingProblem.pluginNotFound(descriptor));
            } else {
                found.sort(sorter);
                final var def = found.get(0);
                addDefinition(def);
                LOGGER.debug("Located plugin [" + def.toString() + "] using locator [" + def.getLocator().getName() + "].");
            }
        }

        compSorter.sort(componentDescriptors);
        targetSorter.sort(targetDescriptors);
    }

    private class CompSortFuncs implements TopologicalSorter.SorterFuncs<ComponentDescriptor, DependencyDescriptor> {

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

    private class TargetSortFuncs implements TopologicalSorter.SorterFuncs<TargetDescriptor, String> {

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

    private void addProblem(PluginLoadingProblem problem) {
        if (problem.isFatal()) {
            fatalProblems.add(problem);
        } else {
            skippedProblems.add(problem);
        }
    }

    public List<ComponentDescriptor> getComponentDefinitions() {
        return Collections.unmodifiableList(componentDescriptors);
    }

    public List<TargetDescriptor> getTargetDefinitions() {
        return Collections.unmodifiableList(targetDescriptors);
    }

    public List<PluginReference> getPluginDefinitions() {
        return Collections.unmodifiableList(pluginReferences);
    }

    public List<PluginLoadingProblem> getFatalProblems() {
        return Collections.unmodifiableList(fatalProblems);
    }

    public List<PluginLoadingProblem> getSkippedProblems() {
        return Collections.unmodifiableList(skippedProblems);
    }

}
