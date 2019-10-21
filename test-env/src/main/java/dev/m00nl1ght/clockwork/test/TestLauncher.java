package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.locator.BootLayerLocator;
import dev.m00nl1ght.clockwork.locator.JarFileLocator;
import dev.m00nl1ght.clockwork.locator.PluginLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;

public class TestLauncher {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final File TEST_PLUGIN_DIR = new File("test-plugin/build/libs/test-plugin-0.1.jar");

    public static void main(String... args) {
        final var locators = new ArrayList<PluginLocator>();
        locators.add(new BootLayerLocator());
        locators.add(new JarFileLocator(TEST_PLUGIN_DIR));
        ClockworkCore.getInstance().loadPlugins(locators);
    }

}