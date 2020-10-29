package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkConfig;
import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.fnder.ModulePathPluginFinder;
import dev.m00nl1ght.clockwork.reader.ManifestPluginReader;
import dev.m00nl1ght.clockwork.test.env.TestEnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManifestReaderLoadTest extends ClockworkTest {

    @Override
    protected ClockworkConfig buildPluginLayerConfig() {
        return ClockworkConfig.builder()
                .addPluginReader(ManifestPluginReader.newConfig("manifest"))
                .addPluginFinder(ModulePathPluginFinder.configBuilder("jars", TestEnvironment.PLUGINS_DIR).build())
                .addWantedPlugin(DependencyDescriptor.buildAnyVersion("test-plugin-a"))
                .build();
    }

    @Test
    public void checkLoaded() {
        assertEquals(core().getState(), ClockworkCore.State.INITIALISED);
    }

}
