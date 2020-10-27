package dev.m00nl1ght.clockwork.reader;

import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.config.Config;
import dev.m00nl1ght.clockwork.config.ImmutableConfig;

public final class PluginReaderConfig {

    private final String name;
    private final String type;
    private final Config params;

    private PluginReaderConfig(String name, String type, Config params) {
        this.name = Arguments.notNullOrBlank(name, "name");
        this.type = Arguments.notNullOrBlank(type, "type");
        this.params = Arguments.notNull(params, "params");
    }

    private PluginReaderConfig(Config data) {
        this.name = data.get("name");
        this.type = data.get("type");
        this.params = data.getSubconfigOrDefault("params", Config.EMPTY);
    }

    public Config asRaw() {
        return ImmutableConfig.builder()
                .putString("name", name)
                .putString("type", type)
                .putSubconfig("params", params)
                .build();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Config getParams() {
        return params;
    }

    public static PluginReaderConfig from(Config data) {
        return new PluginReaderConfig(data);
    }

    public static PluginReaderConfig of(String name, String type) {
        return new PluginReaderConfig(name, type, Config.EMPTY);
    }

    public static PluginReaderConfig of(String name, String type, Config params) {
        return new PluginReaderConfig(name, type, params);
    }

}
