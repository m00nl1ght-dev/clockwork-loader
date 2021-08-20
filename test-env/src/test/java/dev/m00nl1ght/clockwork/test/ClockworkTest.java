package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.component.ComponentType;
import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.loader.ClockworkConfig;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.fnder.impl.ModuleLayerPluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.impl.ModulePathPluginFinder;
import dev.m00nl1ght.clockwork.loader.reader.impl.ManifestPluginReader;
import dev.m00nl1ght.clockwork.test.env.TestEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ClockworkTest {

    private ClockworkCore clockworkCore;
    private ComponentType<TestEnvironment, ClockworkCore> envComponentType;

    @BeforeAll
    public void setup() {
        final var bootLayerConfig = buildBootLayerConfig().build();
        final var bootLayerLoader = ClockworkLoader.build(bootLayerConfig);
        final var bootLayerCore = bootLayerLoader.loadAndInit();
        final var pluginLayerConfig = buildPluginLayerConfig().build();
        final var pluginLayerLoader = ClockworkLoader.build(bootLayerCore, pluginLayerConfig);
        this.clockworkCore = pluginLayerLoader.load();
        this.envComponentType = clockworkCore.getComponentTypeOrThrow(TestEnvironment.class, ClockworkCore.class);
        this.envComponentType.setFactory(this::buildEnvironment);
        pluginLayerLoader.init();
        this.setupComplete(pluginLayerLoader);
    }

    protected ClockworkConfig.Builder buildBootLayerConfig() {
        return ClockworkConfig.builder()
                .addPluginReader(ManifestPluginReader.newConfig("manifest"))
                .addPluginFinder(ModuleLayerPluginFinder.newConfig("boot", false))
                .addWantedPlugin(DependencyDescriptor.buildAnyVersion("clockwork"))
                .addWantedPlugin(DependencyDescriptor.buildAnyVersion("test-env"))
                .addWantedPlugin(DependencyDescriptor.buildAnyVersion("cwl-annotations"))
                .addWantedPlugin(DependencyDescriptor.buildAnyVersion("cwl-nightconfig"));
    }

    protected ClockworkConfig.Builder buildPluginLayerConfig() {
        return ClockworkConfig.builder()
                .addPluginReader(ManifestPluginReader.newConfig("manifest"))
                .addPluginFinder(ModulePathPluginFinder.newConfig("jars", TestEnvironment.PLUGINS_DIR, false))
                .addWantedPlugin(DependencyDescriptor.buildAnyVersion("test-plugin-a"));
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
