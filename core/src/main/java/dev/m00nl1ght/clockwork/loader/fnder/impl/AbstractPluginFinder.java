package dev.m00nl1ght.clockwork.loader.fnder.impl;

import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.Config.Type;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec.Entry;
import dev.m00nl1ght.clockwork.utils.config.ConfiguredFeatures;

import java.util.List;

public abstract class AbstractPluginFinder implements PluginFinder {

    public static final ConfigSpec CONFIG_SPEC = ConfigSpec.create("internal.pluginfinder", ConfiguredFeatures.CONFIG_SPEC);
    public static final Entry<List<String>> CONFIG_ENTRY_READERS = CONFIG_SPEC.put("readers", Config.LIST);
    public static final Entry<Boolean> CONFIG_ENTRY_WILDCARD = CONFIG_SPEC.put("wildcard", Config.BOOLEAN).defaultValue(false);
    public static final Type<Config> CONFIG_TYPE = CONFIG_SPEC.buildType();

    protected final String name;
    protected final List<String> readerNames;
    protected final boolean wildcard;

    protected AbstractPluginFinder(ClockworkLoader loader, Config config) {
        this.name = config.get(ConfiguredFeatures.CONFIG_ENTRY_NAME);
        this.readerNames = config.get(CONFIG_ENTRY_READERS);
        this.wildcard = config.get(CONFIG_ENTRY_WILDCARD);
    }

    @Override
    public boolean isWildcard() {
        return wildcard;
    }

}
