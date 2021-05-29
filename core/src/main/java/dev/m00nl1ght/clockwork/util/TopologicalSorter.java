package dev.m00nl1ght.clockwork.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public abstract class TopologicalSorter<N, D> {

    private final Map<String, Node<N>> nodes = new HashMap<>();

    public void add(N obj) {
        final var present = nodes.putIfAbsent(idFor(obj), new Node<>(obj));
        if (present != null) onDuplicateId(obj, present.obj);
    }

    public void sort(LinkedList<N> destList) {
        for (var n : nodes.values()) linkNode(n);
        for (var n : nodes.values()) processNode(n, destList);
    }

    private void linkNode(final Node<N> node) {
        for (var d : depsFor(node.obj)) {
            final var dnode = nodes.get(idOfDep(d));
            if (dnode != null && isDepSatisfied(node.obj, d, dnode.obj)) {
                dnode.depOf.add(node);
            } else {
                node.skip = true;
                onMissingDep(node.obj, d, dnode == null ? null : dnode.obj);
            }
        }
    }

    private void processNode(final Node<N> node, final LinkedList<N> sorted) {
        switch (node.flag) {
            case RESOLVING -> onCycleFound(node.obj);
            case PENDING -> {
                node.flag = Flag.RESOLVING;
                if (node.skip) {
                    for (var n : node.depOf) {
                        n.skip = true;
                        onSkippedDep(n.obj, node.obj);
                        processNode(n, sorted);
                    }
                } else {
                    for (var n : node.depOf) processNode(n, sorted);
                    sorted.addFirst(node.obj);
                }
                node.flag = Flag.RESOLVED;
            }
        }
    }

    private static class Node<T> {
        private Node(T obj) {this.obj = obj;}
        private final T obj;
        private final LinkedList<Node<T>> depOf = new LinkedList<>();
        private Flag flag = Flag.PENDING;
        private boolean skip;
    }

    private enum Flag {
        PENDING, RESOLVING, RESOLVED
    }

    protected abstract String idFor(N obj);

    protected abstract String idOfDep(D obj);

    protected abstract boolean isDepSatisfied(N node, D dep, N present);

    protected abstract Iterable<D> depsFor(N obj);

    protected abstract void onDuplicateId(N node, N present);

    protected abstract void onCycleFound(N tail);

    protected abstract void onMissingDep(N node, D dep, N present);

    protected abstract void onSkippedDep(N node, N present);

}
