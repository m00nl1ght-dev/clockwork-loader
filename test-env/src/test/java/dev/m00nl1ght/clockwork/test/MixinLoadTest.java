package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.loader.ClockworkConfig;
import dev.m00nl1ght.clockwork.loader.fnder.impl.ModuleLayerPluginFinder;
import dev.m00nl1ght.clockwork.loader.reader.impl.ManifestPluginReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("MixinLoadTest")
public class MixinLoadTest extends ClockworkTest {

    @Override
    protected ClockworkConfig.Builder buildBootLayerConfig() {
        return ClockworkConfig.builder()
                .addPluginReader(ManifestPluginReader.newConfig("manifest"))
                .addPluginFinder(ModuleLayerPluginFinder.configBuilder("boot").build())
                .addWantedPlugin(DependencyDescriptor.buildAnyVersion("clockwork"))
                .addWantedPlugin(DependencyDescriptor.buildAnyVersion("test-env"))
                .addWantedPlugin(DependencyDescriptor.buildAnyVersion("cwl-annotations"))
                .addWantedPlugin(DependencyDescriptor.buildAnyVersion("cwl-mixin"));
    }

    @Test
    public void checkLoaded() {
        assertEquals(core().getState(), ClockworkCore.State.INITIALISED);
    }

}
