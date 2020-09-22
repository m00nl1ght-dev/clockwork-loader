package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.ImmutableConfig;

import java.util.Map;
import java.util.Set;

public final class LocatorConfig extends ImmutableConfig {

    private final String locator;
    private final Set<String> readers;
    private final boolean wildcard;

    public static Builder builder(String locator) {
        return new Builder(Arguments.notNullOrBlank(locator, "locator"));
    }

    private LocatorConfig(Builder builder) {
        this(builder.locator, builder.getEntries(), builder.readers, builder.wildcard);
    }

    public LocatorConfig(String locator, Map<String, String> params, Set<String> readers, boolean wildcard) {
        super(params, "LocatorConfig[" + locator + "]");
        this.locator = Arguments.notNullOrBlank(locator, "locator");
        this.readers = readers == null ? null : Set.copyOf(readers);
        this.wildcard = wildcard;
    }

    public String getLocator() {
        return locator;
    }

    public Set<String> getReaders() {
        return readers;
    }

    public boolean isWildcard() {
        return wildcard;
    }

    public static class Builder extends ImmutableConfig.Builder {

        private final String locator;
        private Set<String> readers;
        private boolean wildcard;

        private Builder(String locator) {
            this.locator = locator;
        }

        @Override
        public LocatorConfig build() {
            return new LocatorConfig(this);
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
