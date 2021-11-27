package dev.m00nl1ght.clockwork.loader;

import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.loader.jigsaw.JigsawStrategy;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec;
import dev.m00nl1ght.clockwork.utils.config.ConfiguredFeatures;

import java.nio.file.Path;
import java.util.List;

import static dev.m00nl1ght.clockwork.utils.config.ConfigValue.*;

public final class ClockworkConfig extends ConfigSpec {

    public static final String SPEC_NAME = "clockwork_config";

    public final Entry<List<DependencyDescriptor>>              WANTED_PLUGINS = entry("plugins",
            T_LIST_U(DependencyDescriptor.T_VALUE))             .defaultValue();

    public final Entry<List<Config>>                            PLUGIN_FINDERS = entry("pluginFinders",
            T_CLIST_F(ConfiguredFeatures.SPEC))                 .defaultValue();

    public final Entry<List<Config>>                            PLUGIN_READERS = entry("pluginReaders",
            T_CLIST_F(ConfiguredFeatures.SPEC))                 .defaultValue(List.of(PluginReader.DEFAULT));

    public final Entry<Config>                                  JIGSAW_STRATEGY = entry("jigsawStrategy",
            T_CONFIG(ConfiguredFeatures.SPEC))                  .defaultValue(JigsawStrategy.DEFAULT);

    public final Entry<List<Config>>                            CLASS_TRANSFORMERS = entry("classTransformers",
            T_CLIST_F(ConfiguredFeatures.SPEC))                 .defaultValue();

    public final Entry<List<Path>>                              LIB_MODULE_PATH = entry("libModulePath",
            T_LIST_UF(T_PATH))                                  .defaultValue();

    public final Entry<Config>                                  EXT_CONFIG = entry("ext",
            T_CONFIG)                                           .defaultValue();

    public static final ClockworkConfig SPEC = new ClockworkConfig();
    private ClockworkConfig() {
        super(SPEC_NAME, true);
        initialize();
    }

}
