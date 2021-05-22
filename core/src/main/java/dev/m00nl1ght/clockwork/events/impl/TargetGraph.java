package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.LinkingComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class TargetGraph {

    public static final Node<?> EMPTY_NODE = new Node<>();

    protected final Map<TargetType<?>, Node<?>> nodeMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends ComponentTarget> @NotNull Node<T> get(@NotNull TargetType<T> targetType) {

        final var existing = (Node<T>) nodeMap.get(targetType);
        if (existing != null) return existing;

        final Set<LinkingComponentType<?, T>> links = targetType.getOwnComponentTypes().stream()
                .filter(c -> c instanceof LinkingComponentType)
                .map(c -> (LinkingComponentType<?, T>) c)
                .collect(Collectors.toUnmodifiableSet());

        if (links.isEmpty()) {
            final var empty = (Node<T>) EMPTY_NODE;
            nodeMap.put(targetType, empty);
            return empty;
        }

        final var node = new Node<T>();
        node.providing = links;
        nodeMap.put(targetType, node);
        links.forEach(this::applyLink);
        return node;
    }

    private <A extends ComponentTarget, B extends ComponentTarget> void applyLink(LinkingComponentType<A, B> link) {
        final var node = get(link.getInnerTargetType());
        if (node == EMPTY_NODE) {
            final var newNode = new Node<A>();
            newNode.providedBy = Set.of(link);
            nodeMap.put(link.getInnerTargetType(), newNode);
        } else {
            final var newSet = new HashSet<>(node.providedBy);
            newSet.add(link);
            node.providedBy = Set.copyOf(newSet);
        }
    }

    public static class Node<T extends ComponentTarget> {

        protected Set<LinkingComponentType<T, ?>> providedBy = Collections.emptySet();
        protected Set<LinkingComponentType<?, T>> providing = Collections.emptySet();

        public Set<LinkingComponentType<T, ?>> getProvidedBy() {
            return providedBy;
        }

        public Set<LinkingComponentType<?, T>> getProviding() {
            return providing;
        }

    }

}
