package dev.m00nl1ght.clockwork.extension.nightconfig;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import dev.m00nl1ght.clockwork.config.Config;
import dev.m00nl1ght.clockwork.config.ImmutableConfig;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class NightconfigWrapper implements Config {

    public static Config load(File file) {
        final var config = FileConfig.of(file);
        config.load(); config.close();
        return new NightconfigWrapper(config.unmodifiable());
    }

    private final UnmodifiableConfig config;

    public NightconfigWrapper(UnmodifiableConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public Set<String> getKeys() {
        return config.valueMap().keySet();
    }

    @Override
    public String getOrNull(String key) {
        return config.get(key);
    }

    @Override
    public Config getSubconfigOrNull(String key) {
        final UnmodifiableConfig sub = config.get(key);
        return sub == null ? null : new NightconfigWrapper(sub);
    }

    @Override
    public List<String> getListOrNull(String key) {
        return config.get(key);
    }

    @Override
    public List<Config> getSubconfigListOrNull(String key) {
        final List<UnmodifiableConfig> subs = config.get(key);
        return subs == null ? null : subs.stream()
                .map(NightconfigWrapper::new)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Config immutable() {
        final var builder = ImmutableConfig.builder();

        for (final var entry : config.entrySet()) {
            final var value = entry.getValue();
            if (value instanceof List) {
                final var list = (List<?>) value;
                if (list.stream().allMatch(e -> UnmodifiableConfig.class.isAssignableFrom(e.getClass()))) {
                    builder.putSubconfigs(entry.getKey(), list.stream()
                            .map(config -> new NightconfigWrapper((UnmodifiableConfig) config))
                            .collect(Collectors.toUnmodifiableSet()));
                } else {
                    builder.putStrings(entry.getKey(), list.stream()
                            .map(Object::toString)
                            .collect(Collectors.toUnmodifiableSet()));
                }
            } else if (value instanceof UnmodifiableConfig) {
                builder.putSubconfig(entry.getKey(), new NightconfigWrapper((UnmodifiableConfig) value));
            } else {
                builder.putString(entry.getKey(), value.toString());
            }
        }

        return builder.build();
    }

}
