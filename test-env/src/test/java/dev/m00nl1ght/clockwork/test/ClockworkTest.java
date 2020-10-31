package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkConfig;
import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.fnder.ModuleLayerPluginFinder;
import dev.m00nl1ght.clockwork.fnder.ModulePathPluginFinder;
import dev.m00nl1ght.clockwork.reader.ManifestPluginReader;
import dev.m00nl1ght.clockwork.test.env.TestEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

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
        pluginLayerLoader.collectExtensionsFromParent();
        this.clockworkCore = pluginLayerLoader.load();
        this.envComponentType = clockworkCore.getComponentType(TestEnvironment.class, ClockworkCore.class).orElseThrow();
        this.envComponentType.setFactory(this::buildEnvironment);
        pluginLayerLoader.init();
    }

    protected ClockworkConfig buildBootLayerConfig() {
        final var configBuilder = ClockworkConfig.builder();
        configBuilder.addPluginReader(ManifestPluginReader.newConfig("manifest"));
        configBuilder.addPluginFinder(ModuleLayerPluginFinder.configBuilder("boot").wildcard().build());
        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("clockwork"));
        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("test-env"));
        return configBuilder.build();
    }

    protected ClockworkConfig buildPluginLayerConfig() {
        return ClockworkConfig.builder()
                .addPluginReader(ManifestPluginReader.newConfig("manifest"))
                .addPluginFinder(ModulePathPluginFinder.configBuilder("jars", TestEnvironment.PLUGINS_DIR).build())
                .addWantedPlugin(DependencyDescriptor.buildAnyVersion("test-plugin-a"))
                .build();
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
        return new TestEnvironment();
    }

}
