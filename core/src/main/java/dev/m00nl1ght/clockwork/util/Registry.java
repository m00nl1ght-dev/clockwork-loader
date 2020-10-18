package dev.m00nl1ght.clockwork.util;

import java.util.HashMap;
import java.util.Map;

public class Registry<T> {

    protected final Map<String, T> registered = new HashMap<>();
    protected final Class<T> regType;

    public Registry(Class<T> regType) {
        this.regType = Arguments.notNull(regType, "regType");
    }

    public synchronized void register(String id, T object) {
        final var existing = registered.putIfAbsent(id, object);
        if (existing != null) throw FormatUtil.rtExc("[] with id [] is already registered", regType.getSimpleName(), id);
    }

    public T get(String id) {
        final var object = registered.get(id);
        if (object == null) throw FormatUtil.rtExc("No [] with id [] is registered", regType.getSimpleName(), id);
        return object;
    }

    public Map<String, T> getRegistered() {
        return Map.copyOf(registered);
    }

}
