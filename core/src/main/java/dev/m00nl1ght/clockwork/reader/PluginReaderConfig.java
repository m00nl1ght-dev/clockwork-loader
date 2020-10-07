package dev.m00nl1ght.clockwork.reader;

import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.ImmutableConfig;

import java.util.Map;

public final class PluginReaderConfig extends ImmutableConfig {

    private final String name;
    private final String type;

    public static Builder builder(String name, String type) {
        return new Builder(Arguments.notNullOrBlank(name, "name"), Arguments.notNullOrBlank(type, "type"));
    }

    private PluginReaderConfig(Builder builder) {
        this(builder.name, builder.type, builder.getEntries());
    }

    public PluginReaderConfig(String name, String type, Map<String, String> params) {
        super(params, "ReaderConfig[" + name + "]");
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
            configName("ReaderConfig[" + name + "]");
        }

        @Override
        public PluginReaderConfig build() {
            return new PluginReaderConfig(this);
        }

    }

}
