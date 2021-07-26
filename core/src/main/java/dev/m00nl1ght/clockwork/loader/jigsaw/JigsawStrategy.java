package dev.m00nl1ght.clockwork.loader.jigsaw;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.classloading.ClassTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface JigsawStrategy {

    @NotNull Map<@NotNull PluginReference, @NotNull ModuleLayer>
    buildModuleLayers(@NotNull Collection<@NotNull PluginReference> plugins,
                      @NotNull Set<@NotNull Path> libModulePath,
                      @NotNull List<@NotNull ClassTransformer> transformers,
                      @Nullable ClockworkCore parent);

}
