package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.locator.JarFileLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class TestLauncher {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final File TEST_PLUGIN_DIR = new File("test-plugin/build/libs/test-plugin-0.1.jar");

    public static void main(String... args) {
        ClockworkCore.getInstance().registerLocator(new JarFileLocator(TEST_PLUGIN_DIR));
        ClockworkCore.getInstance().loadPlugins();
    }

}
