package dev.m00nl1ght.clockwork.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class MapToSet<K, V> extends AbstractMap<K, Set<V>> {

    private final Map<K, Set<V>> map = new HashMap<>();

    @NotNull
    @Override
    public Set<Entry<K, Set<V>>> entrySet() {
        return map.entrySet();
    }

    public boolean addValue(K key, V value) {
        final var values = map.getOrDefault(key, Set.of());
        if (values.contains(value)) return false;
        @SuppressWarnings("unchecked")
        final var newValues = (Set<V>) Set.of(values.toArray(), value);
        map.put(key, newValues);
        return true;
    }

    public boolean removeValue(K key, V value) {
        final var values = new HashSet<>(map.getOrDefault(key, Set.of()));
        if (!values.remove(value)) return false;
        map.put(key, Set.copyOf(values));
        return true;
    }

    public Set<V> getAll() {
        return map.values().stream().flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet());
    }

}
