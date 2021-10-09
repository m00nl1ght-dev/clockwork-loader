package dev.m00nl1ght.clockwork.loader.jigsaw;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.classloading.ClassTransformer;
import dev.m00nl1ght.clockwork.loader.jigsaw.impl.JigsawStrategyFlat;
import dev.m00nl1ght.clockwork.utils.config.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface JigsawStrategy {

    Config DEFAULT = JigsawStrategyFlat.newConfig("flatStrategy");

    @NotNull Map<@NotNull PluginReference, @NotNull ModuleLayer>
    buildModuleLayers(@NotNull Collection<@NotNull PluginReference> plugins,
                      @NotNull List<@NotNull Path> libModulePath,
                      @NotNull List<@NotNull ClassTransformer> transformers,
                      @Nullable ClockworkCore parent);

}
