package dev.m00nl1ght.clockwork.utils.config;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class MapToSet<K, V> extends AbstractMap<K, Set<V>> {

    private final Map<K, Set<V>> map = new HashMap<>();

    @Override
    public @NotNull Set<Entry<K, Set<V>>> entrySet() {
        return map.entrySet();
    }

    public boolean addValue(K key, V value) {
        return map.computeIfAbsent(key, n -> new HashSet<>()).add(value);
    }

    public boolean removeValue(K key, V value) {
        return map.computeIfAbsent(key, n -> new HashSet<>()).remove(value);
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
