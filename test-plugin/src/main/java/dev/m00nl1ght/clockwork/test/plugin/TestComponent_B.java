package dev.m00nl1ght.clockwork.test.plugin;

import dev.m00nl1ght.clockwork.extension.annotations.EventHandler;
import dev.m00nl1ght.clockwork.test.TestInterface;
import dev.m00nl1ght.clockwork.test.TestTarget_B;
import dev.m00nl1ght.clockwork.test.event.TestEvent_A;
import dev.m00nl1ght.clockwork.test.event.TestEvent_B;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestComponent_B implements TestInterface {

    private static final Logger LOGGER = LogManager.getLogger();

    private final TestTarget_B target;

    public TestComponent_B(TestTarget_B target) {
        this.target = target;
    }

    @EventHandler
    protected void onTestEventA(TestEvent_A event) {
        LOGGER.info("TestEvent_A received for " + target.getClass().getSimpleName() + ".");
    }

    @EventHandler
    protected void onTestEventB(TestEvent_B event) {
        LOGGER.info("TestEvent_B received for " + target.getClass().getSimpleName() + ".");
    }

    @Override
    public void tick() {
        LOGGER.info("Ticked " + target.getClass().getSimpleName() + ".");
    }

}
