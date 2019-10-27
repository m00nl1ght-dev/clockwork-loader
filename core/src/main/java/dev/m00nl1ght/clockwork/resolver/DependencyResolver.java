package dev.m00nl1ght.clockwork.resolver;

import dev.m00nl1ght.clockwork.core.ComponentDefinition;
import dev.m00nl1ght.clockwork.core.DependencyDefinition;
import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.core.PluginLoadingProblem;
import dev.m00nl1ght.clockwork.locator.PluginLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class DependencyResolver {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final List<PluginDefinition> pluginDefinitions = new ArrayList<>();
    private final LinkedList<ComponentDefinition> sortedComponents = new LinkedList<>();
    private final List<PluginLoadingProblem> fatalProblems = new ArrayList<>();
    private final List<PluginLoadingProblem> skippedProblems = new ArrayList<>();

    public void addDefinition(PluginDefinition def, PluginLocator loader) {
        addDefinition(def, false);
        LOGGER.debug(loader.getName() + " located plugin [" + def.toString() + "]");
    }

    public void addDefinition(PluginDefinition def, boolean preloaded) {
        pluginDefinitions.add(def);
        def.getComponents().forEach(c -> this.addDefinition(c, preloaded));
    }

    private void addDefinition(ComponentDefinition def, boolean preloaded) {
        final var existing = nodes.get(def.getId());
        if (existing != null) addProblem(PluginLoadingProblem.duplicateIdFound(def, existing.def));
        nodes.put(def.getId(), new Node(def, preloaded));
    }

    public void resolveAndSort() {
        nodes.values().forEach(this::findDeps);
        nodes.values().forEach(this::processNode);
    }

    private void findDeps(Node node) {
        for (var dep : node.def.getDependencies()) {
            final var depNode = nodes.get(dep.getComponentId());
            if (depNode != null && depNode.flag != Flag.SKIPPED && dep.acceptsVersion(depNode.def.getVersion())) {
                depNode.depOf.add(node);
            } else {
                missingDep(node, dep, depNode);
            }
        }
    }

    private void missingDep(Node node, DependencyDefinition dep, Node depNode) {
        if (node.flag == Flag.SKIPPED) return; node.flag = Flag.SKIPPED;
        final var depSkipped = depNode != null && depNode.flag == Flag.SKIPPED;
        addProblem(PluginLoadingProblem.depNotFound(node.def, dep, depNode == null ? null : depNode.def, depSkipped));
        // Also remove any components that are already processed and depend on the skipped one
        for (var nextSkip : node.depOf) {
            var lostDep = nextSkip.def.getDependencies().stream().filter(d -> d.getComponentId().equals(node.def.getId())).findFirst();
            missingDep(nextSkip, lostDep.orElse(null), node);
        }
    }

    private void addProblem(PluginLoadingProblem problem) {
        if (problem.isFatal()) {
            fatalProblems.add(problem);
        } else {
            skippedProblems.add(problem);
        }
    }

    private void processNode(Node node) {
        if (node.flag == Flag.CURRENT) addProblem(PluginLoadingProblem.depCycleFound(node.def));
        if (node.flag != Flag.PENDING) return;
        node.flag = Flag.CURRENT;
        node.depOf.forEach(this::processNode);
        sortedComponents.addFirst(node.def);
        node.flag = Flag.DONE;
    }

    public LinkedList<ComponentDefinition> getLoadingOrder() {
        return sortedComponents;
    }

    public List<PluginDefinition> getPluginDefinitions() {
        return pluginDefinitions;
    }

    public List<PluginLoadingProblem> getFatalProblems() {
        return Collections.unmodifiableList(fatalProblems);
    }

    public List<PluginLoadingProblem> getSkippedProblems() {
        return Collections.unmodifiableList(skippedProblems);
    }

    private static class Node {
        private Node(ComponentDefinition def, boolean preloaded) {this.def = def; this.flag = preloaded ? Flag.DONE : Flag.PENDING;}
        private Node(ComponentDefinition def) {this(def, false);}
        private final ComponentDefinition def;
        private final LinkedList<Node> depOf = new LinkedList<>();
        private Flag flag;
    }

    private enum Flag {
        PENDING, CURRENT, DONE, SKIPPED
    }

}
