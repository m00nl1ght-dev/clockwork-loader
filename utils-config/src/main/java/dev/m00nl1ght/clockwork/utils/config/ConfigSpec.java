package dev.m00nl1ght.clockwork.utils.config;

import dev.m00nl1ght.clockwork.utils.config.Config.Type;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ConfigSpec {

    protected final ConfigSpec extendsFrom;
    protected final Map<String, Entry<?>> entryMap;

    protected boolean locked;

    public static ConfigSpec create() {
        return new ConfigSpec(null);
    }

    public static ConfigSpec create(ConfigSpec extendsFrom) {
        return new ConfigSpec(extendsFrom.lock());
    }

    protected ConfigSpec(ConfigSpec extendsFrom) {
        this.extendsFrom = extendsFrom;
        this.entryMap = new HashMap<>();
        if (extendsFrom != null) {
            this.entryMap.putAll(extendsFrom.entryMap);
        }
    }

    public <T> Entry<T> add(String key, Type<T> valueType) {
        this.requireNotLocked();
        final var entry = new Entry<>(this, key, valueType, entryMap.size());
        if (entryMap.putIfAbsent(key, entry) != null) {
            throw new RuntimeException("ConfigSpec already contains entry for key: " + key);
        } else {
            return entry;
        }
    }

    public Entry<?> getEntry(String key) {
        return entryMap.get(key);
    }

    public Set<Entry<?>> getEntries() {
        return Set.copyOf(entryMap.values());
    }

    public ConfigSpec getExtendsFrom() {
        return extendsFrom;
    }

    public ConfigSpec lock() {
        this.locked = true;
        return this;
    }

    public boolean isLocked() {
        return locked;
    }

    protected void requireNotLocked() {
        if (this.locked) throw new IllegalStateException("ConfigSpec is already in use and can no longer be modified");
    }

    public static class Entry<T> implements Comparable<Entry<?>> {

        protected final ConfigSpec spec;
        protected final String key;
        protected final Type<T> type;
        protected final int sortIndex;

        protected boolean required;
        protected T defaultValue;

        protected Entry(ConfigSpec spec, String key, Type<T> type, int sortIndex) {
            this.spec = Objects.requireNonNull(spec);
            this.key = Objects.requireNonNull(key);
            this.type = Objects.requireNonNull(type);
            this.sortIndex = sortIndex;
        }

        public Entry<T> defaultValue(T defaultValue) {
            spec.requireNotLocked();
            this.defaultValue = defaultValue;
            return this;
        }

        public T getDefaultValue() {
            return defaultValue;
        }

        public Entry<T> required() {
            return required(true);
        }

        public Entry<T> required(boolean required) {
            spec.requireNotLocked();
            this.required = required;
            return this;
        }

        public boolean isRequired() {
            return required;
        }

        public final String getKey() {
            return key;
        }

        public final Type<T> getType() {
            return type;
        }

        public int getSortIndex() {
            return sortIndex;
        }

        @Override
        public int compareTo(@NotNull ConfigSpec.Entry<?> o) {
            return Integer.compare(sortIndex, o.sortIndex);
        }

    }

}
