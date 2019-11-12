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

    private void linkNode(final Node<T> node) { // TODO check if prev deps skipped?
        if (node.flag == Flag.LINKED) return; node.flag = Flag.LINKED;
        for (var d : sorterFuncs.depsFor(node.obj)) {
            final var dnode = nodes.get(sorterFuncs.idOfDep(d));
            if (dnode == null) {
                if (sorterFuncs.onMissingDep(node.obj, d, null, false)) node.flag = Flag.SKIPPED;
            } else if (dnode.flag == Flag.SKIPPED) {
                if (sorterFuncs.onMissingDep(node.obj, d, dnode.obj, true)) node.flag = Flag.SKIPPED;
            } else if (!sorterFuncs.isDepSatisfied(node.obj, d, dnode.obj)) {
                if (sorterFuncs.onMissingDep(node.obj, d, dnode.obj, false)) node.flag = Flag.SKIPPED;
            } else {
                linkNode(dnode);
                node.depOf.add(dnode);
            }
        }
    }

    private void processNode(final Node<T> node, final LinkedList<T> sorted) {
        if (node.flag == Flag.RESOLVING) sorterFuncs.onCycleFound(node.obj);
        if (node.flag != Flag.PENDING) return;
        node.flag = Flag.RESOLVING;
        for (var n : node.depOf) processNode(n, sorted);
        sorted.addFirst(node.obj);
        node.flag = Flag.RESOLVED;
    }

    private static class Node<T> {
        private Node(T obj) {this.obj = obj;}
        private final T obj;
        private final LinkedList<Node<T>> depOf = new LinkedList<>();
        private Flag flag = Flag.PENDING;
    }

    private enum Flag {
        PENDING, LINKED, RESOLVING, RESOLVED, SKIPPED
    }

    public interface SorterFuncs<T, D> {

        String idFor(T obj);

        String idOfDep(D obj);

        boolean isDepSatisfied(T node, D dep, T present);

        Iterable<D> depsFor(T obj);

        void onCycleFound(T tail);

        boolean onMissingDep(T node, D dep, T present, boolean skiped);

    }

}
