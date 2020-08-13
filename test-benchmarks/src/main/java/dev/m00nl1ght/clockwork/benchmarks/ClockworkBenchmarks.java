package dev.m00nl1ght.clockwork.benchmarks;

import dev.m00nl1ght.clockwork.core.*;
import dev.m00nl1ght.clockwork.locator.BootLayerLocator;

public class ClockworkBenchmarks {

    public static final ClockworkCore clockworkCore;
    public static final TargetType<ClockworkCore> coreTargetType;

    static {
        final var configBuilder = ClockworkConfig.builder();
        configBuilder.addPluginLocator(new BootLayerLocator());
        configBuilder.addComponentDescriptor(ComponentDescriptor.buildAnyVersion("clockwork"));
        configBuilder.addComponentDescriptor(ComponentDescriptor.buildAnyVersion("clockwork-benchmarks"));
        clockworkCore = ClockworkCore.load(configBuilder.build());
        coreTargetType = clockworkCore.getTargetType(ClockworkCore.class).orElseThrow();
        clockworkCore.init(new ComponentContainer<>(coreTargetType, clockworkCore));
    }

}
