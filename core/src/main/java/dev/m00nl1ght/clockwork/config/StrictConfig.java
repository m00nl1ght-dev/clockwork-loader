package dev.m00nl1ght.clockwork.config;

import dev.m00nl1ght.clockwork.util.FormatUtil;

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
    public List<Config> getSubconfigListOrNull(String key) {
        queried.add(key);
        return config.getSubconfigListOrNull(key);
    }

    @Override
    public Config immutable() {
        return config.immutable().strict();
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
                throw FormatUtil.rtExc("Config [] contains entry [] that is invalid or inapplicable in this context", this, key);
    }

    @Override
    public StrictConfig strict() {
        return this;
    }

    @Override
    public String toString() {
        return config.toString();
    }

}
