package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.*;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.extension.annotations.CWLAnnotationsExtension;
import dev.m00nl1ght.clockwork.extension.annotations.EventHandlerAnnotationProcessor;
import dev.m00nl1ght.clockwork.locator.JarFileLocator;
import dev.m00nl1ght.clockwork.security.ClockworkSecurityPolicy;
import dev.m00nl1ght.clockwork.security.SecurityConfiguration;
import dev.m00nl1ght.clockwork.security.permissions.FilePermissionEntry;
import dev.m00nl1ght.clockwork.security.permissions.NetworkPermissionEntry;
import dev.m00nl1ght.clockwork.security.permissions.PropertyPermissionEntry;
import dev.m00nl1ght.clockwork.test.event.PluginInitEvent;
import dev.m00nl1ght.clockwork.test.event.SimpleTestEvent;
import dev.m00nl1ght.clockwork.test.event.GenericTestEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

public class TestLauncher {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final File TEST_PLUGIN_JAR = new File("test-plugin/build/libs/");
    private static final File PLUGIN_DATA_DIR = new File("test-env/plugin-data/");

    private static ClockworkCore clockworkCore;
    private static TargetType<ClockworkCore> coreTargetType;

    public static void main(String... args) {
        PLUGIN_DATA_DIR.mkdirs();

        final var securityConfig = new SecurityConfiguration();
        securityConfig.addPermission(new PropertyPermissionEntry(PropertyPermissionEntry.ACTIONS_READ));
        securityConfig.addPermission(new FilePermissionEntry(new File(PLUGIN_DATA_DIR, "$plugin-id$"), FilePermissionEntry.ACTIONS_RWD));
        securityConfig.addPermission(new FilePermissionEntry("file", new File("."), FilePermissionEntry.ACTIONS_RWD));
        securityConfig.addPermission(new NetworkPermissionEntry("network", NetworkPermissionEntry.ACTIONS_CONNECT_ACCEPT));
        ClockworkSecurityPolicy.install(securityConfig);

        final var configBuilder = ClockworkConfig.builder();
        configBuilder.addPluginLocator(new JarFileLocator(TEST_PLUGIN_JAR, JarFileLocator.JarInJarPolicy.ALLOW));
        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("clockwork"));
        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("cwl-annotations"));
        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("test-env"));
        configBuilder.addWantedPlugin(DependencyDescriptor.buildAnyVersion("test-plugin"));

        final var bootLayerLoader = ClockworkLoader.buildBootLayerDefault();
        EventHandlerAnnotationProcessor.registerTo(bootLayerLoader);
        final var bootLayerCore = bootLayerLoader.loadAndInit();

        final var loader = ClockworkLoader.build(bootLayerCore, configBuilder.build());
        loader.collectExtensionsFromParent();

        clockworkCore = loader.load();
        coreTargetType = clockworkCore.getTargetType(ClockworkCore.class).orElseThrow();

        loader.init();

        PluginInitEvent.TYPE.register(coreTargetType);
        SimpleTestEvent.TYPE.register(TestTarget_A.TARGET_TYPE);
        GenericTestEvent.TYPE_STRING.register(TestTarget_B.TARGET_TYPE);
        GenericTestEvent.TYPE_RAW.register(TestTarget_B.TARGET_TYPE);
        TestInterface.TYPE.register(TestTarget_A.TARGET_TYPE, true);

        CWLAnnotationsExtension.buildListeners(clockworkCore, List.of(PluginInitEvent.TYPE, SimpleTestEvent.TYPE, GenericTestEvent.TYPE_STRING, GenericTestEvent.TYPE_RAW));

        PluginInitEvent.TYPE.post(clockworkCore, new PluginInitEvent(clockworkCore, PLUGIN_DATA_DIR));
    }

    public static TargetType<ClockworkCore> getCoreTargetType() {
        if (coreTargetType == null) throw new IllegalStateException("target class has been initialised before clockwork core is ready");
        return coreTargetType;
    }

    public static <T extends ComponentTarget> TargetType<T> getTargetType(Class<T> targetClass) {
        if (clockworkCore == null) throw new IllegalStateException("target class has been initialised before clockwork core is ready");
        return clockworkCore.getTargetType(targetClass)
                .orElseThrow(() -> new IllegalArgumentException("missing target for class: " + targetClass.getSimpleName()));
    }

    public static <C, T extends ComponentTarget> ComponentType<C, T> getComponentType(Class<C> componentClass, Class<T> targetClass) {
        if (clockworkCore == null) throw new IllegalStateException("component class has been initialised before clockwork core is ready");
        return clockworkCore.getComponentType(componentClass, targetClass)
                .orElseThrow(() -> new IllegalArgumentException("missing component for class: " + componentClass.getSimpleName()));
    }

}
