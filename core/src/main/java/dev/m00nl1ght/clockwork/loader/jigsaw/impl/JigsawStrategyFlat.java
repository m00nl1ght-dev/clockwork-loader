package dev.m00nl1ght.clockwork.loader.jigsaw.impl;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.classloading.ClassTransformer;
import dev.m00nl1ght.clockwork.loader.classloading.ClockworkClassLoader;
import dev.m00nl1ght.clockwork.loader.jigsaw.JigsawStrategy;
import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ConfiguredFeatures;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JigsawStrategyFlat implements JigsawStrategy {

    public static final String TYPE = "internal.jigsaw.flat";
    public static final Spec SPEC = new Spec();

    public static void registerTo(ClockworkLoader loader) {
        loader.getFeatureProviders().register(JigsawStrategy.class, TYPE, JigsawStrategyFlat::new);
    }

    public static ModifiableConfig newConfig(String name) {
        return Config.newConfig(SPEC)
                .put(SPEC.FEATURE_TYPE, TYPE)
                .put(SPEC.FEATURE_NAME, Objects.requireNonNull(name));
    }

    protected JigsawStrategyFlat(ClockworkLoader loader, Config config) {}

    @Override
    public @NotNull Map<@NotNull PluginReference, @NotNull ModuleLayer>
    buildModuleLayers(@NotNull Collection<@NotNull PluginReference> plugins,
                      @NotNull List<@NotNull Path> libModulePath,
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

    public static class Spec extends ConfiguredFeatures.Spec {

        protected Spec(String specName) {
            super(specName);
        }

        private Spec() {
            super(TYPE);
            initialize();
        }

    }

}
