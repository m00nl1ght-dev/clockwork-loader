package dev.m00nl1ght.clockwork.utils.config;

import dev.m00nl1ght.clockwork.utils.config.ConfigValue.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigSpec {

    protected final String specName;
    protected final ConfigSpec extendsFrom;
    protected final Map<String, Entry<?>> entryMap;

    protected boolean allowAdditionalEntries;

    protected boolean locked;

    public static ConfigSpec create(String specName) {
        return new ConfigSpec(specName, null);
    }

    public static ConfigSpec create(String specName, ConfigSpec extendsFrom) {
        return new ConfigSpec(specName, extendsFrom.lock());
    }

    protected ConfigSpec(String specName, ConfigSpec extendsFrom) {
        this.specName = Objects.requireNonNull(specName);
        this.extendsFrom = extendsFrom;
        this.entryMap = new HashMap<>();
        if (extendsFrom != null) {
            this.entryMap.putAll(extendsFrom.entryMap);
        }
    }

    public ConfigException verify(Config config, boolean requireCompleteness) {
        this.lock();
        return entryMap.values().stream()
                .map(entry -> verify(config, entry, requireCompleteness))
                .filter(Objects::nonNull).findFirst().orElse(null);
    }

    private <T> ConfigException verify(Config config, Entry<T> entry, boolean requireCompleteness) {
        final var value = config.get(entry.key, entry.type);
        if (value != null) {
            return entry.type.verify(config, entry.key, value);
        } else if (requireCompleteness && entry.required) {
            return new ConfigException(config, "Missing value for required entry " + entry.key + " in " + config);
        } else return null;
    }

    public Set<String> findAdditionalEntries(Config config) {
        this.lock();
        return config.getKeys().stream().filter(k -> !entryMap.containsKey(k)).collect(Collectors.toSet());
    }

    public <T> Entry<T> put(String key, Type<T> valueType) {
        this.requireNotLocked();
        final var existing = entryMap.get(key);
        if (existing != null) {
            if (existing.type.isCompatible(valueType)) {
                final var entry = new Entry<>(this, key, valueType, existing.sortIndex);
                entryMap.put(key, entry);
                return entry;
            } else {
                throw new RuntimeException("ConfigSpec already contains incompatible entry for key: " + key);
            }
        } else {
            final var entry = new Entry<>(this, key, valueType, entryMap.size());
            entryMap.put(key, entry);
            return entry;
        }
    }

    public Entry<?> getEntry(String key) {
        return entryMap.get(key);
    }

    public Set<Entry<?>> getEntries() {
        return Set.copyOf(entryMap.values());
    }

    public ConfigSpec forSubconfig(String key) {
        final var entry = entryMap.get(key);
        if (entry == null) return null;
        if (entry.type instanceof ConfigValue.TypeConfig)
            return ((ConfigValue.TypeConfig) entry.type).spec;
        if (entry.type instanceof ConfigValue.TypeConfigList)
            return ((ConfigValue.TypeConfigList) entry.type).spec;
        return null;
    }

    public void allowAdditionalEntries() {
        this.allowAdditionalEntries(true);
    }

    public void allowAdditionalEntries(boolean allowAdditionalEntries) {
        this.requireNotLocked();
        this.allowAdditionalEntries = allowAdditionalEntries;
    }

    public boolean doesAllowAdditionalEntries() {
        return allowAdditionalEntries;
    }

    public ConfigSpec getExtendsFrom() {
        return extendsFrom;
    }

    public String getName() {
        return specName;
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

    public boolean canApplyAs(@NotNull ConfigSpec other) {
        return other == this || (extendsFrom != null && extendsFrom.canApplyAs(other));
    }

    public static boolean canApply(@Nullable ConfigSpec spec, @Nullable ConfigSpec applyAs) {
        // spec may extend from applyAs
        return applyAs == null || (spec != null && spec.canApplyAs(applyAs));
    }

    public ConfigValue.TypeConfig buildType() {
        return ConfigValue.CONFIG(this);
    }

    @Override
    public String toString() {
        return specName;
    }

    public static class Entry<T> implements Comparable<Entry<?>> {

        protected final ConfigSpec spec;
        protected final String key;
        protected final Type<T> type;
        protected final int sortIndex;

        protected boolean required;
        protected Function<Config, T> defaultSupplier = c -> null;
        protected BinaryOperator<T> mergeFunction;

        protected Entry(ConfigSpec spec, String key, Type<T> type, int sortIndex) {
            this.spec = Objects.requireNonNull(spec);
            this.key = Objects.requireNonNull(key);
            this.type = Objects.requireNonNull(type);
            this.mergeFunction = type.getDefaultMergeFunction();
            this.sortIndex = sortIndex;
        }

        public Entry<T> defaultValue(T defaultValue) {
            spec.requireNotLocked();
            this.defaultSupplier = c -> defaultValue;
            return this;
        }

        public Entry<T> defaultValue() {
            return defaultValue(type.getDefaultValue());
        }

        public Entry<T> defaultTo(Entry<T> other) {
            return defaultTo(other, null);
        }

        public Entry<T> defaultTo(Entry<T> other, T fallback) {
            spec.requireNotLocked();
            if (!spec.canApplyAs(other.spec))
                throw new IllegalArgumentException("Config spec mismatch");
            this.defaultSupplier = c -> c == null ? fallback : c.get(other);
            return this;
        }

        public Entry<T> defaultTo(Function<Config, T> defaultSupplier) {
            spec.requireNotLocked();
            this.defaultSupplier = Objects.requireNonNull(defaultSupplier);
            return this;
        }

        public T getDefaultValue() {
            return getDefaultValue(null);
        }

        public T getDefaultValue(@Nullable Config config) {
            return defaultSupplier.apply(config);
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

        public void mergeWith(BinaryOperator<T> mergeFunction) {
            this.mergeFunction = Objects.requireNonNull(mergeFunction);
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
