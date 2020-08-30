package dev.m00nl1ght.clockwork.benchmarks;

import dev.m00nl1ght.clockwork.core.*;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.extension.annotations.EventHandlerAnnotationProcessor;
import dev.m00nl1ght.clockwork.locator.BootLayerLocator;

public class ClockworkBenchmarks {

    public static final ClockworkCore clockworkCore;

    static {
        final var configBuilder = ClockworkConfig.builder();
        configBuilder.addPluginLocator(new BootLayerLocator());
        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("clockwork"));
        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("cwl-annotations"));
        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("clockwork-benchmarks"));
        final var loader = ClockworkLoader.build(configBuilder.build());
        EventHandlerAnnotationProcessor.registerTo(loader);
        clockworkCore = loader.loadAndInit();
    }

}
