package dev.m00nl1ght.clockwork.resolver;

import dev.m00nl1ght.clockwork.core.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class DependencyResolver {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ClockworkConfig config;

    private final TopologicalSorter<ComponentDefinition, ComponentDescriptor> compSorter = new TopologicalSorter<>(new CompSortFuncs());
    private final TopologicalSorter<TargetDefinition, String> targetSorter = new TopologicalSorter<>(new TargetSortFuncs());
    private final LinkedList<PluginDefinition> pluginDefinitions = new LinkedList<>();
    private final LinkedList<ComponentDefinition> componentDefinitions = new LinkedList<>();
    private final LinkedList<TargetDefinition> targetDefinitions = new LinkedList<>();
    private final List<PluginLoadingProblem> fatalProblems = new ArrayList<>();
    private final List<PluginLoadingProblem> skippedProblems = new ArrayList<>();

    public DependencyResolver(ClockworkConfig config) {
        this.config = config;
    }

    private void addDefinition(PluginDefinition def) {
        pluginDefinitions.add(def);
        def.getTargetDefinitions().forEach(this::addDefinition);
        def.getComponentDefinitions().forEach(this::addDefinition);
    }

    private void addDefinition(ComponentDefinition def) {
        final var present = compSorter.add(def);
        if (present != null) addProblem(PluginLoadingProblem.duplicateIdFound(def, def, present));
    }

    private void addDefinition(TargetDefinition def) {
        final var present = targetSorter.add(def);
        if (present != null) addProblem(PluginLoadingProblem.duplicateIdFound(def.getPlugin().getMainComponent(), def, present));
    }

    public void resolveAndSort() {
        final Comparator<PluginDefinition> sorter = Comparator.comparing(PluginDefinition::getVersion).reversed();

        for (final var descriptor : config.getComponentDescriptors()) {
            final var found = new ArrayList<PluginDefinition>();
            for (var locator : config.getPluginLocators()) {
                for (var def : locator.find(descriptor)) {
                    found.add(def);
                    if (def.getLocator() != locator)
                        addProblem(PluginLoadingProblem.locatorMismatch(def.getMainComponent(), locator));
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

        compSorter.sort(componentDefinitions);
        targetSorter.sort(targetDefinitions);
    }

    private class CompSortFuncs implements TopologicalSorter.SorterFuncs<ComponentDefinition, ComponentDescriptor> {

        @Override
        public String idFor(ComponentDefinition obj) {
            return obj.getId();
        }

        @Override
        public String idOfDep(ComponentDescriptor obj) {
            return obj.getTarget();
        }

        @Override
        public boolean isDepSatisfied(ComponentDefinition node, ComponentDescriptor dep, ComponentDefinition present) {
            return dep.acceptsVersion(present.getVersion());
        }

        @Override
        public Iterable<ComponentDescriptor> depsFor(ComponentDefinition obj) {
            return obj.getDependencies();
        }

        @Override
        public void onCycleFound(ComponentDefinition tail) {
            addProblem(PluginLoadingProblem.depCycleFound(tail, tail));
        }

        @Override
        public void onMissingDep(ComponentDefinition node, ComponentDescriptor dep, ComponentDefinition present) {
            addProblem(PluginLoadingProblem.depNotFound(node, dep, present));
        }

        @Override
        public void onSkippedDep(ComponentDefinition node, ComponentDefinition present) {
            addProblem(PluginLoadingProblem.depSkipped(node, present));
        }

    }

    private class TargetSortFuncs implements TopologicalSorter.SorterFuncs<TargetDefinition, String> {

        @Override
        public String idFor(TargetDefinition obj) {
            return obj.getId();
        }

        @Override
        public String idOfDep(String obj) {
            return obj;
        }

        @Override
        public boolean isDepSatisfied(TargetDefinition node, String dep, TargetDefinition present) {
            return true;
        }

        @Override
        public Iterable<String> depsFor(TargetDefinition obj) {
            return obj.getParent() == null ? Collections.emptySet() : Collections.singleton(obj.getParent());
        }

        @Override
        public void onCycleFound(TargetDefinition tail) {
            addProblem(PluginLoadingProblem.depCycleFound(tail.getPlugin().getMainComponent(), tail));
        }

        @Override
        public void onMissingDep(TargetDefinition node, String required, TargetDefinition present) {
            addProblem(PluginLoadingProblem.parentNotFound(node));
        }

        @Override
        public void onSkippedDep(TargetDefinition node, TargetDefinition present) {
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

    public List<ComponentDefinition> getComponentDefinitions() {
        return Collections.unmodifiableList(componentDefinitions);
    }

    public List<TargetDefinition> getTargetDefinitions() {
        return Collections.unmodifiableList(targetDefinitions);
    }

    public List<PluginDefinition> getPluginDefinitions() {
        return Collections.unmodifiableList(pluginDefinitions);
    }

    public List<PluginLoadingProblem> getFatalProblems() {
        return Collections.unmodifiableList(fatalProblems);
    }

    public List<PluginLoadingProblem> getSkippedProblems() {
        return Collections.unmodifiableList(skippedProblems);
    }

}
