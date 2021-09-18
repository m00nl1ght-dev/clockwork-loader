package dev.m00nl1ght.clockwork.utils.config;

import java.util.*;
import java.util.function.Predicate;

public class StrictConfig implements Config {

    private final Config config;
    private final Set<String> queried = new HashSet<>();

    protected StrictConfig(Config config) {
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public Set<String> getKeys() {
        return config.getKeys();
    }

    @Override
    public String getOrNull(String key) {
        queried.add(key);
        return config.getOrNull(key);
    }

    @Override
    public Config getSubconfigOrNull(String key) {
        queried.add(key);
        return config.getSubconfigOrNull(key);
    }

    @Override
    public List<String> getListOrNull(String key) {
        queried.add(key);
        return config.getListOrNull(key);
    }

    @Override
    public List<? extends Config> getSubconfigListOrNull(String key) {
        queried.add(key);
        return config.getSubconfigListOrNull(key);
    }

    @Override
    public Config copy() {
        return config.copy();
    }

    @Override
    public ModifiableConfig modifiableCopy() {
        return config.modifiableCopy();
    }

    public Set<String> getQueried() {
        return Set.copyOf(queried);
    }

    public void throwOnRemaining() {
        throwOnRemaining(s -> true);
    }

    public void throwOnRemaining(Predicate<String> condition) {
        for (final var key : config.getKeys())
            if (!queried.contains(key) && condition.test(key))
                throw new RuntimeException("Config " + this + " contains entry " + key + " that is invalid or inapplicable in this context");
    }

    @Override
    public StrictConfig asStrict() {
        return this;
    }

    @Override
    public String toString() {
        return config.toString();
    }

}
