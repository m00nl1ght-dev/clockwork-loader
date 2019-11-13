package dev.m00nl1ght.clockwork.resolver;

import dev.m00nl1ght.clockwork.core.*;
import dev.m00nl1ght.clockwork.locator.PluginLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DependencyResolver {

    private static final Logger LOGGER = LogManager.getLogger();

    private final TopologicalSorter<ComponentDefinition, DependencyDefinition> compSorter = new TopologicalSorter<>(new CompSortFuncs());
    private final TopologicalSorter<ComponentTargetDefinition, String> targetSorter = new TopologicalSorter<>(new TargetSortFuncs());
    private final LinkedList<PluginDefinition> pluginDefinitions = new LinkedList<>();
    private final LinkedList<ComponentDefinition> componentDefinitions = new LinkedList<>();
    private final LinkedList<ComponentTargetDefinition> targetDefinitions = new LinkedList<>();
    private final List<PluginLoadingProblem> fatalProblems = new ArrayList<>();
    private final List<PluginLoadingProblem> skippedProblems = new ArrayList<>();

    public void addDefinition(PluginDefinition def, PluginLocator loader) {
        pluginDefinitions.add(def);
        def.getTargetDefinitions().forEach(this::addDefinition);
        def.getComponentDefinitions().forEach(this::addDefinition);
        LOGGER.debug(loader.getName() + " located plugin [" + def.toString() + "]");
    }

    public void addDefinition(ComponentDefinition def) {
        final var present = compSorter.add(def);
        if (present != null) addProblem(PluginLoadingProblem.duplicateIdFound(def, def, present));
    }

    public void addDefinition(ComponentTargetDefinition def) {
        final var present = targetSorter.add(def);
        if (present != null) addProblem(PluginLoadingProblem.duplicateIdFound(def.getPlugin().getMainComponent(), def, present));
    }

    public void resolveAndSort() {
        compSorter.sort(componentDefinitions);
        targetSorter.sort(targetDefinitions);
    }

    private class CompSortFuncs implements TopologicalSorter.SorterFuncs<ComponentDefinition, DependencyDefinition> {

        @Override
        public String idFor(ComponentDefinition obj) {
            return obj.getId();
        }

        @Override
        public String idOfDep(DependencyDefinition obj) {
            return obj.getComponentId();
        }

        @Override
        public boolean isDepSatisfied(ComponentDefinition node, DependencyDefinition dep, ComponentDefinition present) {
            return dep.acceptsVersion(present.getVersion());
        }

        @Override
        public Iterable<DependencyDefinition> depsFor(ComponentDefinition obj) {
            return obj.getDependencies();
        }

        @Override
        public void onCycleFound(ComponentDefinition tail) {
            addProblem(PluginLoadingProblem.depCycleFound(tail, tail));
        }

        @Override
        public void onMissingDep(ComponentDefinition node, DependencyDefinition dep, ComponentDefinition present) {
            addProblem(PluginLoadingProblem.depNotFound(node, dep, present));
        }

        @Override
        public void onSkippedDep(ComponentDefinition node, ComponentDefinition present) {
            addProblem(PluginLoadingProblem.depSkipped(node, present));
        }

    }

    private class TargetSortFuncs implements TopologicalSorter.SorterFuncs<ComponentTargetDefinition, String> {

        @Override
        public String idFor(ComponentTargetDefinition obj) {
            return obj.getId();
        }

        @Override
        public String idOfDep(String obj) {
            return obj;
        }

        @Override
        public boolean isDepSatisfied(ComponentTargetDefinition node, String dep, ComponentTargetDefinition present) {
            return true;
        }

        @Override
        public Iterable<String> depsFor(ComponentTargetDefinition obj) {
            return obj.getParent() == null ? Collections.emptySet() : Collections.singleton(obj.getParent());
        }

        @Override
        public void onCycleFound(ComponentTargetDefinition tail) {
            addProblem(PluginLoadingProblem.depCycleFound(tail.getPlugin().getMainComponent(), tail));
        }

        @Override
        public void onMissingDep(ComponentTargetDefinition node, String required, ComponentTargetDefinition present) {
            addProblem(PluginLoadingProblem.parentNotFound(node));
        }

        @Override
        public void onSkippedDep(ComponentTargetDefinition node, ComponentTargetDefinition present) {
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

    public List<ComponentTargetDefinition> getTargetDefinitions() {
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
