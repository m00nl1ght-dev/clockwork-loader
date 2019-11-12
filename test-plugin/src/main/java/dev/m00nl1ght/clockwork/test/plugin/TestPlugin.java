package dev.m00nl1ght.clockwork.test.plugin;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.event.EventHandler;
import dev.m00nl1ght.clockwork.test.event.PluginInitEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;

public class TestPlugin {

    private static final Logger LOGGER = LogManager.getLogger();
    private final ClockworkCore core;

    public TestPlugin(ClockworkCore core) {
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

}
