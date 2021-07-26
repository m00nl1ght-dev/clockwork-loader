package dev.m00nl1ght.clockwork.loader.jigsaw.impl;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.loader.classloading.ClassTransformer;
import dev.m00nl1ght.clockwork.loader.classloading.ClockworkClassLoader;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.jigsaw.JigsawStrategy;
import dev.m00nl1ght.clockwork.loader.jigsaw.JigsawStrategyConfig;
import dev.m00nl1ght.clockwork.loader.jigsaw.JigsawStrategyType;
import dev.m00nl1ght.clockwork.util.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class JigsawStrategyFlat implements JigsawStrategy {

    public static final String NAME = "internal.jigsaw.flat";
    public static final JigsawStrategyType FACTORY = JigsawStrategyFlat::new;

    protected final JigsawStrategyConfig config;

    public static void registerTo(Registry<JigsawStrategyType> registry) {
        Objects.requireNonNull(registry).register(NAME, FACTORY);
    }

    protected JigsawStrategyFlat(JigsawStrategyConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public @NotNull Map<@NotNull PluginReference, @NotNull ModuleLayer>
    buildModuleLayers(@NotNull Collection<@NotNull PluginReference> plugins,
                      @NotNull Set<@NotNull Path> libModulePath,
                      @NotNull List<@NotNull ClassTransformer> transformers,
                      @Nullable ClockworkCore parent) {

        final var parentLayers = parent == null ? List.of(ModuleLayer.boot()) : parent.getModuleLayers();
        final var modules = plugins.stream().map(PluginReference::getModuleName).collect(Collectors.toUnmodifiableList());
        final var finders = plugins.stream().map(PluginReference::getModuleFinder).collect(Collectors.toUnmodifiableList());
        final var pluginMF = ModuleFinder.compose(finders.toArray(ModuleFinder[]::new));
        final var libraryMF = ModuleFinder.of(libModulePath.toArray(Path[]::new));
        final var combinedMF = ModuleFinder.compose(pluginMF, libraryMF);
        final var parentConfs = parentLayers.stream().map(ModuleLayer::configuration).collect(Collectors.toList());
        final var config = Configuration.resolveAndBind(ModuleFinder.of(), parentConfs, combinedMF, modules);
        final var classLoader = new ClockworkClassLoader(config, parentLayers, transformers);
        final var controller = ModuleLayer.defineModules(config, parentLayers, m -> classLoader);
        final var localModule = ClockworkLoader.class.getModule();
        for (final var module : controller.layer().modules())
            for (var packageName : module.getPackages())
                controller.addOpens(module, packageName, localModule);
        return plugins.stream().collect(Collectors.toMap(Function.identity(), p -> controller.layer()));
    }

}
