package dev.m00nl1ght.clockwork.test.plugin.a;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.util.Loggers;
import org.apache.logging.log4j.Logger;

public class TestPlugin_A {

    private static final Logger LOGGER = Loggers.getLogger("TestPlugin_A");

    private final ClockworkCore core;

    public TestPlugin_A(ClockworkCore core) {
        this.core = core;
    }

}
