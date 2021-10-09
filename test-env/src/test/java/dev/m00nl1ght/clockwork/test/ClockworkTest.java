package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.component.ComponentType;
import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.loader.ClockworkConfig;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.fnder.impl.ModuleLayerPluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.impl.ModulePathPluginFinder;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.test.env.TestEnvironment;
import dev.m00nl1ght.clockwork.utils.config.Config;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ClockworkTest {

    private ClockworkCore clockworkCore;
    private ComponentType<TestEnvironment, ClockworkCore> envComponentType;

    @BeforeAll
    public void setup() {
        final var bootLayerConfig = buildBootLayerConfig();
        final var bootLayerLoader = ClockworkLoader.build(bootLayerConfig);
        final var bootLayerCore = bootLayerLoader.loadAndInit();
        final var pluginLayerConfig = buildPluginLayerConfig();
        final var pluginLayerLoader = ClockworkLoader.build(bootLayerCore, pluginLayerConfig);
        this.clockworkCore = pluginLayerLoader.load();
        this.envComponentType = clockworkCore.getComponentTypeOrThrow(TestEnvironment.class, ClockworkCore.class);
        this.envComponentType.setFactory(this::buildEnvironment);
        pluginLayerLoader.init();
        this.setupComplete(pluginLayerLoader);
    }

    protected Config buildBootLayerConfig() {
        final var config = Config.newConfig(ClockworkConfig.SPEC);
        config.put(ClockworkConfig.PLUGIN_READERS, List.of(PluginReader.DEFAULT));
        config.put(ClockworkConfig.PLUGIN_FINDERS, List.of(ModuleLayerPluginFinder.newConfig("boot", false)));
        config.put(ClockworkConfig.WANTED_PLUGINS, List.of(
                DependencyDescriptor.buildAnyVersion("clockwork"),
                DependencyDescriptor.buildAnyVersion("test-env"),
                DependencyDescriptor.buildAnyVersion("cwl-annotations"),
                DependencyDescriptor.buildAnyVersion("cwl-nightconfig")));
        return config;
    }

    protected Config buildPluginLayerConfig() {
        final var config = Config.newConfig(ClockworkConfig.SPEC);
        config.put(ClockworkConfig.PLUGIN_READERS, List.of(PluginReader.DEFAULT));
        config.put(ClockworkConfig.PLUGIN_FINDERS, List.of(ModulePathPluginFinder.newConfig("jars", TestEnvironment.PLUGINS_DIR, false)));
        config.put(ClockworkConfig.WANTED_PLUGINS, List.of(DependencyDescriptor.buildAnyVersion("test-plugin-a")));
        return config;
    }

    public ClockworkCore core() {
        if (clockworkCore == null) throw new IllegalStateException("setup() did not run");
        return clockworkCore;
    }

    public TestEnvironment env() {
        if (envComponentType == null) throw new IllegalStateException("setup() did not run");
        return envComponentType.get(clockworkCore);
    }

    protected TestEnvironment buildEnvironment(ClockworkCore core) {
        return new TestEnvironment(core);
    }

    protected void setupComplete(ClockworkLoader loader) {}

}
