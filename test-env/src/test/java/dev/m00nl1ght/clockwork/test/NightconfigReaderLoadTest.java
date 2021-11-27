package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.extension.nightconfig.NightconfigPluginReader;
import dev.m00nl1ght.clockwork.loader.ClockworkConfig;
import dev.m00nl1ght.clockwork.loader.fnder.impl.ModulePathPluginFinder;
import dev.m00nl1ght.clockwork.test.env.TestEnvironment;
import dev.m00nl1ght.clockwork.utils.config.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NightconfigReaderLoad")
public class NightconfigReaderLoadTest extends ClockworkTest {

    @Override
    protected Config buildPluginLayerConfig() {
        final var config = Config.newConfig(ClockworkConfig.SPEC);
        config.put(ClockworkConfig.SPEC.PLUGIN_READERS, List.of(NightconfigPluginReader.newConfig("toml", "META-INF/plugin.toml")));
        config.put(ClockworkConfig.SPEC.PLUGIN_FINDERS, List.of(ModulePathPluginFinder.newConfig("jars", TestEnvironment.PLUGINS_DIR.toPath(), false)));
        config.put(ClockworkConfig.SPEC.WANTED_PLUGINS, List.of(DependencyDescriptor.buildAnyVersion("test-plugin-a")));
        return config;
    }

    @Test
    public void checkLoaded() {
        assertEquals(core().getPhase(), ClockworkCore.Phase.INITIALISED);
    }

}
