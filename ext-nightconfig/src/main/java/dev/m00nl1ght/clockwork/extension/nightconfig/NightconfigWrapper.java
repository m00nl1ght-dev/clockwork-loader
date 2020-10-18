package dev.m00nl1ght.clockwork.extension.nightconfig;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import dev.m00nl1ght.clockwork.core.ClockworkConfig;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.config.Config;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NightconfigWrapper implements Config {

    public static ClockworkConfig loadClockworkConfig(File file) {
        final var config = FileConfig.of(file);
        config.load(); config.close();
        return ClockworkConfig.from(new NightconfigWrapper(config.unmodifiable()));
    }

    private final UnmodifiableConfig config;

    public NightconfigWrapper(UnmodifiableConfig config) {
        this.config = Arguments.notNull(config, "config");
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

}
