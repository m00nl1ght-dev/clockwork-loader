package dev.m00nl1ght.clockwork.verifier;

import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.ImmutableConfig;

import java.util.Map;

public final class PluginVerifierConfig extends ImmutableConfig {

    private final String name;
    private final String type;

    public static Builder builder(String name, String type) {
        return new Builder(Arguments.notNullOrBlank(name, "name"), Arguments.notNullOrBlank(type, "type"));
    }

    private PluginVerifierConfig(Builder builder) {
        this(builder.name, builder.type, builder.getEntries());
    }

    public PluginVerifierConfig(String name, String type, Map<String, String> params) {
        super(params, "VerifierConfig[" + name + "]");
        this.name = Arguments.notNullOrBlank(name, "name");
        this.type = Arguments.notNullOrBlank(type, "type");
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public static class Builder extends ImmutableConfig.Builder {

        private final String name;
        private final String type;

        private Builder(String name, String type) {
            this.name = name;
            this.type = type;
            configName("VerifierConfig[" + name + "]");
        }

        @Override
        public PluginVerifierConfig build() {
            return new PluginVerifierConfig(this);
        }

    }

}
