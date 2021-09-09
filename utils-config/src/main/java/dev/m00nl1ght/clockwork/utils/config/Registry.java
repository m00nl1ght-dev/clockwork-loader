package dev.m00nl1ght.clockwork.utils.config;

import java.util.*;

public class Registry<T> {

    protected final Map<String, T> registered = new HashMap<>();
    protected final String elementName;

    public Registry(String elementName) {
        this.elementName = Objects.requireNonNull(elementName);
    }

    public Registry(Class<T> regType) {
        this.elementName = Objects.requireNonNull(regType).getSimpleName();
    }

    public synchronized void register(String id, T object) {
        final var existing = registered.putIfAbsent(Objects.requireNonNull(id), Objects.requireNonNull(object));
        if (existing != null) throw new RuntimeException(elementName + " with id " + id + " is already registered");
    }

    public T get(String id) {
        final var object = registered.get(Objects.requireNonNull(id));
        if (object == null) throw new RuntimeException("No " + elementName + " with id " + id + " is registered");
        return object;
    }

    public Optional<T> getOptional(String id) {
        return Optional.ofNullable(registered.get(Objects.requireNonNull(id)));
    }

    public Map<String, T> getRegisteredAsMap() {
        return Map.copyOf(registered);
    }

    public Set<T> getRegistered() {
        return Set.copyOf(registered.values());
    }

}
