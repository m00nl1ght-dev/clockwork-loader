package dev.m00nl1ght.clockwork.test.plugin.a;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.extension.annotations.EventHandler;
import dev.m00nl1ght.clockwork.test.TestTarget_A;
import dev.m00nl1ght.clockwork.test.TestTarget_B;
import dev.m00nl1ght.clockwork.test.event.PluginInitEvent;
import dev.m00nl1ght.clockwork.test.event.SimpleTestEvent;
import dev.m00nl1ght.clockwork.util.Loggers;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;

public class TestPlugin_A {

    private static final Logger LOGGER = Loggers.getLogger("TestPlugin_A");
    private final ClockworkCore core;

    public TestPlugin_A(ClockworkCore core) {
        this.core = core;
    }

    @EventHandler
    public void onInit(PluginInitEvent event) {
        LOGGER.info("Init event received.");
        final var dataDir = event.getDataDirectory(this);
        dataDir.mkdirs();
        final var file = new File(dataDir, "test.txt");
        try {file.createNewFile();} catch (Exception e) {throw new RuntimeException("oof", e);}
        try (var fileWriter = new FileWriter(file)) {
            fileWriter.write("test string from plugin");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public static void staticOnSimpleTestEventByComponentA(TestComponent_A component, SimpleTestEvent event) {
        LOGGER.info("SimpleTestEvent received by static listener on Component A.");
    }

    @EventHandler
    public static void staticOnSimpleTestEventByComponentA(TestComponent_B component, SimpleTestEvent event) {
        LOGGER.info("SimpleTestEvent received by static listener on Component B.");
    }

    @EventHandler
    public static void staticOnSimpleTestEventByTargetA(TestTarget_A target, SimpleTestEvent event) {
        LOGGER.info("SimpleTestEvent received by static listener on Target A.");
    }

    @EventHandler
    public static void staticOnSimpleTestEventByTargetB(TestTarget_B target, SimpleTestEvent event) {
        LOGGER.info("SimpleTestEvent received by static listener on Target B.");
    }

}
