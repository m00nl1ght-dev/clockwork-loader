package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.fnder.ModuleLayerPluginFinder;
import dev.m00nl1ght.clockwork.fnder.ModulePathPluginFinder;
import dev.m00nl1ght.clockwork.fnder.NestedPluginFinder;
import dev.m00nl1ght.clockwork.fnder.PluginFinderType;
import dev.m00nl1ght.clockwork.jigsaw.JigsawStrategyFlat;
import dev.m00nl1ght.clockwork.jigsaw.JigsawStrategyType;
import dev.m00nl1ght.clockwork.reader.ManifestPluginReader;
import dev.m00nl1ght.clockwork.reader.PluginReaderType;
import dev.m00nl1ght.clockwork.util.Registry;
import dev.m00nl1ght.clockwork.verifier.PluginVerifierType;
import org.jetbrains.annotations.NotNull;

public class ExtensionContext {

    private final Registry<PluginReaderType> readerTypeRegistry = new Registry<>(PluginReaderType.class);
    private final Registry<PluginFinderType> finderTypeRegistry = new Registry<>(PluginFinderType.class);
    private final Registry<PluginVerifierType> verifierTypeRegistry = new Registry<>(PluginVerifierType.class);
    private final Registry<PluginProcessor> processorRegistry = new Registry<>(PluginProcessor.class);
    private final Registry<JigsawStrategyType> jigsawTypeRegistry = new Registry<>(JigsawStrategyType.class);

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

}
