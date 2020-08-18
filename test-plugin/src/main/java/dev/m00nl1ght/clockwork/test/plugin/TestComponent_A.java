package dev.m00nl1ght.clockwork.test.plugin;

import dev.m00nl1ght.clockwork.extension.eventhandler.EventHandler;
import dev.m00nl1ght.clockwork.test.TestInterface;
import dev.m00nl1ght.clockwork.test.TestTarget_A;
import dev.m00nl1ght.clockwork.test.event.TestEvent_A;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestComponent_A implements TestInterface {

    private static final Logger LOGGER = LogManager.getLogger();

    private final TestTarget_A target;

    public TestComponent_A(TestTarget_A target) {
        this.target = target;
    }

    @EventHandler
    protected void onTestEventA(TestEvent_A event) {
        LOGGER.info("TestEvent_A received for " + target.getClass().getSimpleName() + ".");
    }

    @Override
    public void tick() {
        LOGGER.info("Ticked " + target.getClass().getSimpleName() + ".");
    }

}
