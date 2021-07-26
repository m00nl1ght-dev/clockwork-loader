package dev.m00nl1ght.clockwork.loader;

import dev.m00nl1ght.clockwork.loader.fnder.impl.ModuleLayerPluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.impl.ModulePathPluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.impl.NestedPluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.PluginFinderType;
import dev.m00nl1ght.clockwork.loader.jigsaw.impl.JigsawStrategyFlat;
import dev.m00nl1ght.clockwork.loader.jigsaw.JigsawStrategyType;
import dev.m00nl1ght.clockwork.loader.processor.PluginProcessor;
import dev.m00nl1ght.clockwork.loader.reader.impl.ManifestPluginReader;
import dev.m00nl1ght.clockwork.loader.reader.PluginReaderType;
import dev.m00nl1ght.clockwork.loader.classloading.ClassTransformer;
import dev.m00nl1ght.clockwork.util.Registry;
import dev.m00nl1ght.clockwork.loader.verifier.PluginVerifierType;
import org.jetbrains.annotations.NotNull;

public class ExtensionContext {

    private final Registry<PluginReaderType> readerTypeRegistry = new Registry<>(PluginReaderType.class);
    private final Registry<PluginFinderType> finderTypeRegistry = new Registry<>(PluginFinderType.class);
    private final Registry<PluginVerifierType> verifierTypeRegistry = new Registry<>(PluginVerifierType.class);
    private final Registry<PluginProcessor> processorRegistry = new Registry<>(PluginProcessor.class);
    private final Registry<JigsawStrategyType> jigsawTypeRegistry = new Registry<>(JigsawStrategyType.class);
    private final Registry<ClassTransformer> transformerRegistry = new Registry<>(ClassTransformer.class);

    public ExtensionContext(boolean registerDefaults) {
        if (registerDefaults) {
            ManifestPluginReader.registerTo(readerTypeRegistry);
            ModuleLayerPluginFinder.registerTo(finderTypeRegistry);
            ModulePathPluginFinder.registerTo(finderTypeRegistry);
            NestedPluginFinder.registerTo(finderTypeRegistry);
            JigsawStrategyFlat.registerTo(jigsawTypeRegistry);
        }
    }

    public @NotNull Registry<PluginReaderType> getReaderTypeRegistry() {
        return readerTypeRegistry;
    }

    public @NotNull Registry<PluginFinderType> getFinderTypeRegistry() {
        return finderTypeRegistry;
    }

    public @NotNull Registry<PluginVerifierType> getVerifierTypeRegistry() {
        return verifierTypeRegistry;
    }

    public @NotNull Registry<PluginProcessor> getProcessorRegistry() {
        return processorRegistry;
    }

    public @NotNull Registry<JigsawStrategyType> getJigsawTypeRegistry() {
        return jigsawTypeRegistry;
    }

    public @NotNull Registry<ClassTransformer> getTransformerRegistry() {
        return transformerRegistry;
    }

}
