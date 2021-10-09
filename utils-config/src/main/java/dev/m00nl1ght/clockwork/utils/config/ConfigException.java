package dev.m00nl1ght.clockwork.utils.config;

import java.util.Objects;

public class ConfigException extends RuntimeException {

    private final Config config;

    public ConfigException(Config config, String key, Object value, String append, Exception cause) {
        this(config, "Value " + value + " for entry " + key + " in " + config + " " + append, cause);
    }

    public ConfigException(Config config, String key, Object value, String append) {
        this(config, key, value, append, null);
    }

    public ConfigException(Config config, String key, Config value, String append, Exception cause) {
        this(config, "Subconfig for entry " + key + " in " + config + " " + append);
    }

    public ConfigException(Config config, String key, Config value, String append) {
        this(config, key, value, append, null);
    }

    public ConfigException(Config config, String message, Exception cause) {
        super(message, cause);
        this.config = Objects.requireNonNull(config);
    }

    public ConfigException(Config config, String message) {
        this(config, message, null);
    }

    public Config getConfig() {
        return config;
    }

}
