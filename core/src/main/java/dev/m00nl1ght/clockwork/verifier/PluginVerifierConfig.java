package dev.m00nl1ght.clockwork.verifier;

import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.config.Config;
import dev.m00nl1ght.clockwork.config.ImmutableConfig;

public final class PluginVerifierConfig {

    private final String name;
    private final String type;
    private final Config params;

    private PluginVerifierConfig(String name, String type, Config params) {
        this.name = Arguments.notNullOrBlank(name, "name");
        this.type = Arguments.notNullOrBlank(type, "type");
        this.params = Arguments.notNull(params, "params");
    }

    private PluginVerifierConfig(Config data) {
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

    public static PluginVerifierConfig from(Config data) {
        return new PluginVerifierConfig(data);
    }

    public static PluginVerifierConfig of(String name, String type) {
        return new PluginVerifierConfig(name, type, Config.EMPTY);
    }

    public static PluginVerifierConfig of(String name, String type, Config params) {
        return new PluginVerifierConfig(name, type, params);
    }

}
