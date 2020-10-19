package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.*;
import dev.m00nl1ght.clockwork.debug.DebugUtils;
import dev.m00nl1ght.clockwork.debug.profiler.DebugProfiler;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.events.impl.EventBusImpl;
import dev.m00nl1ght.clockwork.extension.annotations.EventHandlerAnnotationProcessor;
import dev.m00nl1ght.clockwork.extension.annotations.ExtEventBusImpl;
import dev.m00nl1ght.clockwork.extension.nightconfig.NightconfigPluginReader;
import dev.m00nl1ght.clockwork.fnder.ModulePathPluginFinder;
import dev.m00nl1ght.clockwork.security.SecurityConfiguration;
import dev.m00nl1ght.clockwork.security.permissions.FilePermissionEntry;
import dev.m00nl1ght.clockwork.security.permissions.NetworkPermissionEntry;
import dev.m00nl1ght.clockwork.security.permissions.PropertyPermissionEntry;
import dev.m00nl1ght.clockwork.test.event.PluginInitEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Set;

public class TestLauncher {

    // TODO rework all of this to actual junit tests

    private static final Logger LOGGER = LogManager.getLogger();

    private static final File TEST_PLUGIN_JAR = new File("test-env/build/libs/");
    private static final File PLUGIN_DATA_DIR = new File("test-env/plugin-data/");

    private static ClockworkCore clockworkCore;
    private static TargetType<ClockworkCore> coreTargetType;
    private static ExtEventBusImpl eventBus;

    public static void main(String... args) {
        PLUGIN_DATA_DIR.mkdirs();

        final var securityConfig = new SecurityConfiguration();
        securityConfig.addPermission(new PropertyPermissionEntry(PropertyPermissionEntry.ACTIONS_READ));
        securityConfig.addPermission(new FilePermissionEntry(new File(PLUGIN_DATA_DIR, "$plugin-id$"), FilePermissionEntry.ACTIONS_RWD));
        securityConfig.addPermission(new FilePermissionEntry("file", new File("."), FilePermissionEntry.ACTIONS_RWD));
        securityConfig.addPermission(new NetworkPermissionEntry("network", NetworkPermissionEntry.ACTIONS_CONNECT_ACCEPT));

        // ClockworkSecurityPolicy.install(); // TODO fully implement policy and permission system

        final var configBuilder = ClockworkConfig.builder();

        configBuilder.addPluginReader(NightconfigPluginReader
                .newConfig("toml", "META-INF/plugin.toml"));
        configBuilder.addPluginFinder(ModulePathPluginFinder
                .configBuilder("testJar", TEST_PLUGIN_JAR)
                .withReaders(Set.of("toml")).build());

        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("clockwork"));
        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("cwl-annotations"));
        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("cwl-nightconfig"));
        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("test-env"));
        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("test-plugin-a"));

        final var bootLayerLoader = ClockworkLoader.buildBootLayerDefault();
        EventHandlerAnnotationProcessor.registerTo(bootLayerLoader.getProcessorRegistry());
        final var bootLayerCore = bootLayerLoader.loadAndInit();

        final var loader = ClockworkLoader.build(bootLayerCore, configBuilder.build());
        loader.collectExtensionsFromParent();

        clockworkCore = loader.load();
        coreTargetType = clockworkCore.getTargetType(ClockworkCore.class).orElseThrow();
        eventBus = new ExtEventBusImpl(clockworkCore);

        loader.init();
        eventBus.bind();

        final var profiler = new DebugProfiler();
        profiler.addGroups(eventBus.attachDefaultProfilers());

        PluginInitEvent.TYPE.post(clockworkCore, new PluginInitEvent(clockworkCore, PLUGIN_DATA_DIR));

        System.out.println(DebugUtils.printProfilerInfo(profiler));
    }

    public static ClockworkCore core() {
        if (clockworkCore == null) throw new IllegalStateException("class has been initialised before clockwork core is ready");
        return clockworkCore;
    }

    public static TargetType<ClockworkCore> coreTargetType() {
        if (clockworkCore == null) throw new IllegalStateException("class has been initialised before clockwork core is ready");
        return coreTargetType;
    }

    public static EventBusImpl eventBus() {
        if (eventBus == null) throw new IllegalStateException("class has been initialised before clockwork core is ready");
        return eventBus;
    }

    public static <T extends ComponentTarget> TargetType<T> targetType(Class<T> targetClass) {
        if (clockworkCore == null) throw new IllegalStateException("class has been initialised before clockwork core is ready");
        return clockworkCore.getTargetType(targetClass)
                .orElseThrow(() -> new IllegalArgumentException("missing target for class: " + targetClass.getSimpleName()));
    }

    public static <C, T extends ComponentTarget> ComponentType<C, T> componentType(Class<C> componentClass, Class<T> targetClass) {
        if (clockworkCore == null) throw new IllegalStateException("class has been initialised before clockwork core is ready");
        return clockworkCore.getComponentType(componentClass, targetClass)
                .orElseThrow(() -> new IllegalArgumentException("missing component for class: " + componentClass.getSimpleName()));
    }

}
