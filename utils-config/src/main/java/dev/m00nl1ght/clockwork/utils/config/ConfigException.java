package dev.m00nl1ght.clockwork.utils.config;

import java.util.Objects;

public class ConfigException extends RuntimeException {

    private final Config config;

    public ConfigException(Config config, String key, Object value, String append) {
        this(config, "Value " + value + " for entry " + key + " in " + config + " " + append);
    }

    public ConfigException(Config config, String key, Config value, String append) {
        this(config, "Subconfig for entry " + key + " in " + config + " " + append);
    }

    public ConfigException(Config config, String message) {
        super(message);
        this.config = Objects.requireNonNull(config);
    }

    public Config getConfig() {
        return config;
    }

}
