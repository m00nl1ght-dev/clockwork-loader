package dev.m00nl1ght.clockwork.resolver;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class TopologicalSorter<T, D> {

    private final Map<String, Node<T>> nodes = new HashMap<>();
    private final SorterFuncs<T, D> sorterFuncs;

    public TopologicalSorter(SorterFuncs<T, D> sorterFuncs) {
        this.sorterFuncs = sorterFuncs;
    }

    public T add(T obj) {
        final var present = nodes.putIfAbsent(sorterFuncs.idFor(obj), new Node<>(obj));
        return present == null ? null : present.obj;
    }

    public void sort(LinkedList<T> destList) {
        for (var n : nodes.values()) linkNode(n);
        for (var n : nodes.values()) processNode(n, destList);
    }

    private void linkNode(final Node<T> node) {
        for (var d : sorterFuncs.depsFor(node.obj)) {
            final var dnode = nodes.get(sorterFuncs.idOfDep(d));
            if (dnode != null && sorterFuncs.isDepSatisfied(node.obj, d, dnode.obj)) {
                dnode.depOf.add(node);
            } else {
                node.flag = Flag.SKIPPED;
                sorterFuncs.onMissingDep(node.obj, d, dnode == null ? null : dnode.obj);
            }
        }
    }

    private void processNode(final Node<T> node, final LinkedList<T> sorted) {
        switch (node.flag) {
            case RESOLVING:
                sorterFuncs.onCycleFound(node.obj);
                break;
            case PENDING:
                node.flag = Flag.RESOLVING;
                for (var n : node.depOf) processNode(n, sorted);
                sorted.addFirst(node.obj);
                node.flag = Flag.RESOLVED;
                break;
            case SKIPPED:
                node.flag = Flag.ABSENT;
                for (var n : node.depOf) {
                    n.flag = Flag.SKIPPED;
                    sorterFuncs.onSkippedDep(node.obj, n.obj);
                    processNode(n, sorted);
                }
        }
    }

    private static class Node<T> {
        private Node(T obj) {this.obj = obj;}
        private final T obj;
        private final LinkedList<Node<T>> depOf = new LinkedList<>();
        private Flag flag = Flag.PENDING;
    }

    private enum Flag {
        PENDING, RESOLVING, RESOLVED, ABSENT, SKIPPED
    }

    public interface SorterFuncs<T, D> {

        String idFor(T obj);

        String idOfDep(D obj);

        boolean isDepSatisfied(T node, D dep, T present);

        Iterable<D> depsFor(T obj);

        void onCycleFound(T tail);

        void onMissingDep(T node, D dep, T present);

        void onSkippedDep(T node, T present);

    }

}
