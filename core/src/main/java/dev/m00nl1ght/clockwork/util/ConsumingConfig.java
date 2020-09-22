package dev.m00nl1ght.clockwork.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ConsumingConfig implements Config {

    private final Map<String, String> entries;
    private final String configName;

    public ConsumingConfig(Config other) {
        Arguments.notNull(other, "other");
        this.entries = new LinkedHashMap<>(other.getEntries());
        this.configName = other.toString();
    }

    public void throwOnRemaining(Predicate<String> condition) {
        for (final var entry : entries.keySet())
            if (condition.test(entry))
                throw FormatUtil.rtExc("Config [] contains entry [] which is invalid or inapplicable in this context", this, entry);
    }

    @Override
    public Map<String, String> getEntries() {
        return Collections.unmodifiableMap(entries);
    }

    @Override
    public String getOrNull(String key) {
        return entries.remove(key);
    }

    @Override
    public String toString() {
        return configName;
    }

}
