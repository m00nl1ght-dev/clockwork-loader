package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.*;
import dev.m00nl1ght.clockwork.debug.DebugUtils;
import dev.m00nl1ght.clockwork.debug.profiler.core.CoreProfiler;
import dev.m00nl1ght.clockwork.locator.BootLayerLocator;
import dev.m00nl1ght.clockwork.locator.JarFileLocator;
import dev.m00nl1ght.clockwork.locator.PluginLocator;
import dev.m00nl1ght.clockwork.security.ClockworkSecurityPolicy;
import dev.m00nl1ght.clockwork.security.SecurityConfiguration;
import dev.m00nl1ght.clockwork.security.permissions.FilePermissionEntry;
import dev.m00nl1ght.clockwork.security.permissions.NetworkPermissionEntry;
import dev.m00nl1ght.clockwork.security.permissions.PropertyPermissionEntry;
import dev.m00nl1ght.clockwork.test.event.PluginInitEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;

public class TestLauncher {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final File TEST_PLUGIN_JAR = new File("test-plugin/build/libs/test-plugin-0.1.jar");
    private static final File PLUGIN_DATA_DIR = new File("test-env/plugin-data/");

    private static ClockworkCore clockworkCore;
    private static TargetType<ClockworkCore> coreTargetType;

    public static void main(String... args) {
        PLUGIN_DATA_DIR.mkdirs();
        final var config = new SecurityConfiguration();
        config.addPermission(new PropertyPermissionEntry(PropertyPermissionEntry.ACTIONS_READ));
        config.addPermission(new FilePermissionEntry(new File(PLUGIN_DATA_DIR, "$plugin-id$"), FilePermissionEntry.ACTIONS_RWD));
        config.addPermission(new FilePermissionEntry("file", new File("."), FilePermissionEntry.ACTIONS_RWD));
        config.addPermission(new NetworkPermissionEntry("network", NetworkPermissionEntry.ACTIONS_CONNECT_ACCEPT));
        ClockworkSecurityPolicy.install(config);
        final var locators = new ArrayList<PluginLocator>();
        locators.add(new BootLayerLocator());
        locators.add(new JarFileLocator(TEST_PLUGIN_JAR, JarFileLocator.JarInJarPolicy.ALLOW));
        clockworkCore = ClockworkCore.load(locators);
        clockworkCore.init();
        coreTargetType = clockworkCore.getTargetType(ClockworkCore.class).orElseThrow();
        final var profiler = new CoreProfiler(clockworkCore, "core");
        PluginInitEvent.TYPE.post(clockworkCore, new PluginInitEvent(clockworkCore, PLUGIN_DATA_DIR, profiler));
        System.out.println(DebugUtils.printProfilerInfo(profiler));
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
