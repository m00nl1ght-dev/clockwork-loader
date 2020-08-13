package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.*;
import dev.m00nl1ght.clockwork.locator.BootLayerLocator;
import dev.m00nl1ght.clockwork.locator.JarFileLocator;
import dev.m00nl1ght.clockwork.security.ClockworkSecurityPolicy;
import dev.m00nl1ght.clockwork.security.SecurityConfiguration;
import dev.m00nl1ght.clockwork.security.permissions.FilePermissionEntry;
import dev.m00nl1ght.clockwork.security.permissions.NetworkPermissionEntry;
import dev.m00nl1ght.clockwork.security.permissions.PropertyPermissionEntry;
import dev.m00nl1ght.clockwork.test.event.PluginInitEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

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
        configBuilder.addPluginLocator(new BootLayerLocator());
        configBuilder.addPluginLocator(new JarFileLocator(TEST_PLUGIN_JAR, JarFileLocator.JarInJarPolicy.ALLOW));
        configBuilder.addComponentDescriptor(ComponentDescriptor.buildAnyVersion("clockwork"));
        configBuilder.addComponentDescriptor(ComponentDescriptor.buildAnyVersion("test-env"));
        configBuilder.addComponentDescriptor(ComponentDescriptor.buildAnyVersion("test-plugin"));

        clockworkCore = ClockworkCore.load(configBuilder.build());
        coreTargetType = clockworkCore.getTargetType(ClockworkCore.class).orElseThrow();

        clockworkCore.init(new ComponentContainer<>(coreTargetType, clockworkCore));
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
