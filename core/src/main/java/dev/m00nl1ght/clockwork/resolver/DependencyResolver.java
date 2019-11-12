package dev.m00nl1ght.clockwork.resolver;

import dev.m00nl1ght.clockwork.core.*;
import dev.m00nl1ght.clockwork.locator.PluginLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;

public class DependencyResolver {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<String, Node<ComponentDefinition>> compNodes = new HashMap<>();
    private final Map<String, Node<ComponentTargetDefinition>> targetNodes = new HashMap<>();
    private final LinkedList<PluginDefinition> pluginDefinitions = new LinkedList<>();
    private final LinkedList<ComponentDefinition> componentDefinitions = new LinkedList<>();
    private final LinkedList<ComponentTargetDefinition> targetDefinitions = new LinkedList<>();
    private final List<PluginLoadingProblem> fatalProblems = new ArrayList<>();
    private final List<PluginLoadingProblem> skippedProblems = new ArrayList<>();

    public void addDefinition(PluginDefinition def, PluginLocator loader) {
        pluginDefinitions.add(def);
        for (var d : def.getTargetDefinitions()) this.addDefinition(d);
        for (var d : def.getComponentDefinitions()) this.addDefinition(d);
        LOGGER.debug(loader.getName() + " located plugin [" + def.toString() + "]");
    }

    public void addDefinition(ComponentDefinition def) {
        final var existing = compNodes.get(def.getId());
        if (existing != null) addProblem(PluginLoadingProblem.duplicateIdFound(def, def, existing.obj));
        compNodes.put(def.getId(), new Node<>(def));
    }

    public void addDefinition(ComponentTargetDefinition def) {
        final var existing = targetNodes.get(def.getId());
        if (existing != null) addProblem(PluginLoadingProblem.duplicateIdFound(def.getPlugin().getMainComponent(), def, existing.obj));
        targetNodes.put(def.getId(), new Node<>(def));
    }

    public void resolveAndSort() {
        for (var n : compNodes.values()) findComponentDeps(n);
        for (var n : compNodes.values()) processNode(n, componentDefinitions, this::onCycleFound);
        for (var n : targetNodes.values()) findTargetDeps(n);
        for (var n : targetNodes.values()) processNode(n, targetDefinitions, this::onCycleFound);
    }

    private void findComponentDeps(Node<ComponentDefinition> node) {
        for (var dep : node.obj.getDependencies()) {
            final var depNode = compNodes.get(dep.getComponentId());
            if (depNode != null && depNode.flag != Flag.SKIPPED && dep.acceptsVersion(depNode.obj.getVersion())) {
                depNode.depOf.add(node);
            } else {
                missingDep(node, dep, depNode);
            }
        }
    }

    private void findTargetDeps(Node<ComponentTargetDefinition> node) {
        if (node.obj.getParent() != null) {
            final var depNode = targetNodes.get(node.obj.getParent());
            if (depNode != null) {
                depNode.depOf.add(node);
            } else {
                addProblem(PluginLoadingProblem.parentNotFound(node.obj));
            }
        }
    }

    private void missingDep(Node<ComponentDefinition> node, DependencyDefinition dep, Node<ComponentDefinition> depNode) {
        if (node.flag == Flag.SKIPPED) return; node.flag = Flag.SKIPPED;
        final var depSkipped = depNode != null && depNode.flag == Flag.SKIPPED;
        addProblem(PluginLoadingProblem.depNotFound(node.obj, dep, depNode == null ? null : depNode.obj, depSkipped));
        // Also remove any components that are already processed and depend on the skipped one
        for (var nextSkip : node.depOf) { // TODO better way without weird recursion (marking conds at end of process method?)
            var lostDep = nextSkip.obj.getDependencies().stream().filter(d -> d.getComponentId().equals(node.obj.getId())).findFirst();
            missingDep(nextSkip, lostDep.orElse(null), node);
        }
    }

    private static <T> void processNode(Node<T> node, LinkedList<T> sorted, Consumer<T> onCycleAction) {
        if (node.flag == Flag.CURRENT) onCycleAction.accept(node.obj);
        if (node.flag != Flag.PENDING) return;
        node.flag = Flag.CURRENT;
        for (var n : node.depOf) processNode(n, sorted, onCycleAction);
        sorted.addFirst(node.obj); // TODO this.. doesnt seem right all of the sudden... wtf? check algorithm!
        node.flag = Flag.DONE;
    }

    private void onCycleFound(ComponentDefinition def) {
        this.addProblem(PluginLoadingProblem.depCycleFound(def, def));
    }

    private void onCycleFound(ComponentTargetDefinition def) {
        this.addProblem(PluginLoadingProblem.depCycleFound(def.getPlugin().getMainComponent(), def));
    }

    private void addProblem(PluginLoadingProblem problem) {
        if (problem.isFatal()) {
            fatalProblems.add(problem);
        } else {
            skippedProblems.add(problem);
        }
    }

    public LinkedList<ComponentDefinition> getComponentDefinitions() {
        return componentDefinitions;
    }

    public LinkedList<ComponentTargetDefinition> getTargetDefinitions() {
        return targetDefinitions;
    }

    public LinkedList<PluginDefinition> getPluginDefinitions() {
        return pluginDefinitions;
    }

    public List<PluginLoadingProblem> getFatalProblems() {
        return Collections.unmodifiableList(fatalProblems);
    }

    public List<PluginLoadingProblem> getSkippedProblems() {
        return Collections.unmodifiableList(skippedProblems);
    }

    private static class Node<T> {
        private Node(T obj) {this.obj = obj;}
        private final T obj;
        private final LinkedList<Node<T>> depOf = new LinkedList<>();
        private Flag flag = Flag.PENDING;
    }

    private enum Flag {
        PENDING, CURRENT, DONE, SKIPPED
    }

}
