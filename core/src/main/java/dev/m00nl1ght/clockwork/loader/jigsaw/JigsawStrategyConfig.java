package dev.m00nl1ght.clockwork.loader.jigsaw;

import dev.m00nl1ght.clockwork.config.Config;
import dev.m00nl1ght.clockwork.config.ImmutableConfig;

import java.util.Objects;

public final class JigsawStrategyConfig {

    private final String type;
    private final Config params;

    private JigsawStrategyConfig(String type, Config params) {
        this.type = Objects.requireNonNull(type);
        this.params = Objects.requireNonNull(params);
    }

    private JigsawStrategyConfig(Config data) {
        this.type = data.getOrDefault("type", JigsawStrategyFlat.NAME);
        this.params = data.getSubconfigOrDefault("params", Config.EMPTY);
    }

    public Config asRaw() {
        return ImmutableConfig.builder()
                .putString("type", type)
                .putSubconfig("params", params)
                .build();
    }

    public String getType() {
        return type;
    }

    public Config getParams() {
        return params;
    }

    public static JigsawStrategyConfig from(Config data) {
        return new JigsawStrategyConfig(data);
    }

    public static JigsawStrategyConfig of(String type) {
        return new JigsawStrategyConfig(type, Config.EMPTY);
    }

    public static JigsawStrategyConfig of(String type, Config params) {
        return new JigsawStrategyConfig(type, params);
    }

}
