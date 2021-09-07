package dev.m00nl1ght.clockwork.utils.collections;

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
        final var values = new HashSet<>(map.getOrDefault(key, Set.of()));
        if (!values.add(value)) return false;
        map.put(key, Set.copyOf(values));
        return true;
    }

    public boolean removeValue(K key, V value) {
        final var values = new HashSet<>(map.getOrDefault(key, Set.of()));
        if (!values.remove(value)) return false;
        map.put(key, Set.copyOf(values));
        return true;
    }

    @Override
    public Set<V> put(K key, Set<V> value) {
        return map.put(key, Objects.requireNonNull(value));
    }

    public Set<V> getValues(K key) {
        return map.getOrDefault(key, Set.of());
    }

    public Set<V> getAll() {
        return map.values().stream().flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet());
    }

}
