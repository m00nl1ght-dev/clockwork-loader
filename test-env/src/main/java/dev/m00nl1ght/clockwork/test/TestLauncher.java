package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;
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
        final var cwc = ClockworkCore.load(locators);
        final var coreTarget = cwc.getTargetType(ClockworkCore.class);
        if (coreTarget.isEmpty()) throw PluginLoadingException.coreTargetMissing(ClockworkCore.CORE_TARGET_ID);
        final var initEvent = coreTarget.get().getEventType(PluginInitEvent.class);
        initEvent.post(cwc, new PluginInitEvent(cwc, PLUGIN_DATA_DIR));
    }

}
