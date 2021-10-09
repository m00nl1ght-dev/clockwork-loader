package dev.m00nl1ght.clockwork.loader;

import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.loader.jigsaw.JigsawStrategy;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.Config.Type;
import dev.m00nl1ght.clockwork.utils.config.Config.TypeParsed;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec.Entry;
import dev.m00nl1ght.clockwork.utils.config.ConfiguredFeatures;

import java.nio.file.Path;
import java.util.*;

public final class ClockworkConfig {

    public static final ConfigSpec SPEC = ConfigSpec.create("clockwork_config");

    public static final TypeParsed<Path>                    TYPE_PATH
            = Config.CUSTOM(Path.class,                     Path::of);

    public static final TypeParsed<DependencyDescriptor>    TYPE_DEPENDENCY
            = Config.CUSTOM(DependencyDescriptor.class,     DependencyDescriptor::buildPlugin);

    public static final Entry<List<DependencyDescriptor>>   WANTED_PLUGINS
            = SPEC.put("plugins",                           Config.LIST_U(TYPE_DEPENDENCY))         .defaultValue();

    public static final Entry<List<Config>>                 PLUGIN_FINDERS
            = SPEC.put("pluginFinders",                     ConfiguredFeatures.CONFIG_LIST_TYPE)    .defaultValue();

    public static final Entry<List<Config>>                 PLUGIN_READERS
            = SPEC.put("pluginReaders",                     ConfiguredFeatures.CONFIG_LIST_TYPE)    .defaultValue(List.of(PluginReader.DEFAULT));

    public static final Entry<List<Config>>                 CLASS_TRANSFORMERS
            = SPEC.put("classTransformers",                 ConfiguredFeatures.CONFIG_LIST_TYPE)    .defaultValue();

    public static final Entry<Config>                       JIGSAW_STRATEGY
            = SPEC.put("jigsawStrategy",                    ConfiguredFeatures.CONFIG_TYPE)         .defaultValue(JigsawStrategy.DEFAULT);

    public static final Entry<List<Path>>                   LIB_MODULE_PATH
            = SPEC.put("libModulePath",                     Config.LIST_UF(TYPE_PATH))              .defaultValue();

    public static final Entry<Config>                       EXT_CONFIG
            = SPEC.put("ext",                               Config.CONFIG)                          .defaultValue();

    public static final Type<Config> TYPE = SPEC.buildType();

    private ClockworkConfig() {}

}
