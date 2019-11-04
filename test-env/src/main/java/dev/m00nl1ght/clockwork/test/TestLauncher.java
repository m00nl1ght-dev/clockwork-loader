package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import dev.m00nl1ght.clockwork.locator.BootLayerLocator;
import dev.m00nl1ght.clockwork.locator.JarFileLocator;
import dev.m00nl1ght.clockwork.locator.PluginLocator;
import dev.m00nl1ght.clockwork.security.ClockworkSecurityPolicy;
import dev.m00nl1ght.clockwork.security.SecurityConfiguration;
import dev.m00nl1ght.clockwork.test.event.PluginInitEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FilePermission;
import java.util.ArrayList;
import java.util.PropertyPermission;

public class TestLauncher {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final File TEST_PLUGIN_DIR = new File("test-plugin/build/libs/test-plugin-0.1.jar");

    public static void main(String... args) {
        final var config = new SecurityConfiguration();
        final var dataDir = new File("test-env/plugin-data/"); dataDir.mkdirs();
        config.addPermission(p -> new FilePermission(new File(dataDir, p.getId()).getAbsolutePath() + "\\-", "read,write,delete"));
        config.addPermission(p -> new PropertyPermission("*", "read"));
        ClockworkSecurityPolicy.install(config);
        final var locators = new ArrayList<PluginLocator>();
        locators.add(new BootLayerLocator());
        locators.add(new JarFileLocator(TEST_PLUGIN_DIR));
        final var cwc = ClockworkCore.load(locators);
        final var coreTarget = cwc.getTargetType(ClockworkCore.class);
        if (coreTarget.isEmpty()) throw PluginLoadingException.generic("core target is missing");
        final var initEvent = coreTarget.get().getEventType(PluginInitEvent.class);
        initEvent.post(cwc, new PluginInitEvent(cwc, dataDir));
    }

}
