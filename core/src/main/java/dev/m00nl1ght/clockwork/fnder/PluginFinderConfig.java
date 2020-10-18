package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.config.Config;
import dev.m00nl1ght.clockwork.util.config.ImmutableConfig;

import java.util.Set;

public final class PluginFinderConfig {

    private final String name;
    private final String type;
    private final Set<String> readers;
    private final Set<String> verifiers;
    private final boolean wildcard;
    private final Config params;

    private PluginFinderConfig(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.readers = builder.readers == null ? null : Set.copyOf(builder.readers);
        this.verifiers = builder.verifiers == null ? null : Set.copyOf(builder.verifiers);
        this.wildcard = builder.wildcard;
        this.params = builder.params;
    }

    private PluginFinderConfig(Config data) {
        this.name = data.get("name");
        this.type = data.get("type");
        this.readers = data.getOptionalList("readers").map(Set::copyOf).orElse(null);
        this.verifiers = data.getOptionalList("verifiers").map(Set::copyOf).orElse(null);
        this.wildcard = data.getBooleanOrDefault("wildcard", false);
        this.params = data.getSubconfigOrDefault("params", Config.EMPTY);
    }

    public Config asRaw() {
        return ImmutableConfig.builder()
                .put("name", name)
                .put("type", type)
                .putList("readers", readers)
                .putList("verifiers", verifiers)
                .put("wildcard", wildcard)
                .put("params", params)
                .build();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Set<String> getReaders() {
        return readers;
    }

    public Set<String> getVerifiers() {
        return verifiers;
    }

    public boolean isWildcard() {
        return wildcard;
    }

    public Config getParams() {
        return params;
    }

    public static PluginFinderConfig from(Config data) {
        return new PluginFinderConfig(data);
    }

    public static Builder builder(String name, String type) {
        return new Builder(Arguments.notNullOrBlank(name, "name"), Arguments.notNullOrBlank(type, "type"));
    }

    public static class Builder {

        private final String name;
        private final String type;
        private Set<String> readers;
        private Set<String> verifiers;
        private boolean wildcard;
        private Config params = Config.EMPTY;

        protected Builder(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public PluginFinderConfig build() {
            return new PluginFinderConfig(this);
        }

        public Builder withReaders(Set<String> readers) {
            this.readers = readers;
            return this;
        }

        public Builder withVerifiers(Set<String> verifiers) {
            this.verifiers = verifiers;
            return this;
        }

        public Builder wildcard(boolean wildcard) {
            this.wildcard = wildcard;
            return this;
        }

        public Builder wildcard() {
            return wildcard(true);
        }

        public Builder withParams(Config params) {
            this.params = params == null ? Config.EMPTY : params;
            return this;
        }

    }

}
