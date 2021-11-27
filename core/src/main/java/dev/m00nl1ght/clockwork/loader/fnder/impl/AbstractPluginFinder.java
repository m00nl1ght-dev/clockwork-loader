package dev.m00nl1ght.clockwork.loader.fnder.impl;

import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ConfiguredFeatures;

import java.util.List;

import static dev.m00nl1ght.clockwork.utils.config.ConfigValue.*;

public abstract class AbstractPluginFinder implements PluginFinder {

    public static final String TYPE = "internal.pluginfinder";
    public static final Spec SPEC = new Spec();

    protected final String name;
    protected final List<String> readerNames;
    protected final boolean wildcard;

    protected AbstractPluginFinder(ClockworkLoader loader, Config config) {
        this.name = config.get(SPEC.FEATURE_NAME);
        this.readerNames = config.get(SPEC.READERS);
        this.wildcard = config.get(SPEC.WILDCARD);
    }

    @Override
    public boolean isWildcard() {
        return wildcard;
    }

    public static class Spec extends ConfiguredFeatures.Spec {

        public final Entry<List<String>>    READERS     = entry("readers", T_LIST);
        public final Entry<Boolean>         WILDCARD    = entry("wildcard", T_BOOLEAN).defaultValue();

        protected Spec(String specName) {
            super(specName);
        }

        private Spec() {
            super(TYPE);
            initialize();
        }

    }
}
