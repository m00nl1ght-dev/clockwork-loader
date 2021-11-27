package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.loader.ClockworkConfig;
import dev.m00nl1ght.clockwork.loader.fnder.impl.ModuleLayerPluginFinder;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.utils.config.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MixinLoadTest")
public class MixinLoadTest extends ClockworkTest {

    @Override
    protected Config buildBootLayerConfig() {
        final var config = Config.newConfig(ClockworkConfig.SPEC);
        config.put(ClockworkConfig.SPEC.PLUGIN_READERS, List.of(PluginReader.DEFAULT));
        config.put(ClockworkConfig.SPEC.PLUGIN_FINDERS, List.of(ModuleLayerPluginFinder.newConfig("boot", false)));
        config.put(ClockworkConfig.SPEC.WANTED_PLUGINS, List.of(
                DependencyDescriptor.buildAnyVersion("clockwork"),
                DependencyDescriptor.buildAnyVersion("test-env"),
                DependencyDescriptor.buildAnyVersion("cwl-annotations"),
                DependencyDescriptor.buildAnyVersion("cwl-mixin")));
        return config;
    }

    @Test
    public void checkLoaded() {
        assertEquals(core().getPhase(), ClockworkCore.Phase.INITIALISED);
    }

}
