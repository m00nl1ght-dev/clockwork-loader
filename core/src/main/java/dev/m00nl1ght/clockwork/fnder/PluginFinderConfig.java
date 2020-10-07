package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.ImmutableConfig;

import java.util.Map;
import java.util.Set;

public final class PluginFinderConfig extends ImmutableConfig {

    private final String name;
    private final String type;
    private final Set<String> readers;
    private final boolean wildcard;

    public static Builder builder(String name, String type) {
        return new Builder(Arguments.notNullOrBlank(name, "name"), Arguments.notNullOrBlank(type, "type"));
    }

    private PluginFinderConfig(Builder builder) {
        this(builder.name, builder.type, builder.getEntries(), builder.readers, builder.wildcard);
    }

    public PluginFinderConfig(String name, String type, Map<String, String> params, Set<String> readers, boolean wildcard) {
        super(params, "LocatorConfig[" + name + "]");
        this.name = Arguments.notNullOrBlank(name, "name");
        this.type = Arguments.notNullOrBlank(type, "type");
        this.readers = readers == null ? null : Set.copyOf(readers);
        this.wildcard = wildcard;
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

    public boolean isWildcard() {
        return wildcard;
    }

    public static class Builder extends ImmutableConfig.Builder {

        private final String name;
        private final String type;
        private Set<String> readers;
        private boolean wildcard;

        private Builder(String name, String type) {
            this.name = name;
            this.type = type;
            configName("LocatorConfig[" + name + "]");
        }

        @Override
        public PluginFinderConfig build() {
            return new PluginFinderConfig(this);
        }

        public void setReaders(Set<String> readers) {
            this.readers = readers;
        }

        public void setWildcard(boolean wildcard) {
            this.wildcard = wildcard;
        }

        public void wildcard() {
            setWildcard(true);
        }

    }

}
