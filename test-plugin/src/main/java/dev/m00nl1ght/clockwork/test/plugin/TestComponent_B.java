package dev.m00nl1ght.clockwork.test.plugin;

import dev.m00nl1ght.clockwork.event.EventHandler;
import dev.m00nl1ght.clockwork.test.TestTarget_B;
import dev.m00nl1ght.clockwork.test.event.TestEvent_A;
import dev.m00nl1ght.clockwork.test.event.TestEvent_B;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestComponent_B {

    private static final Logger LOGGER = LogManager.getLogger();

    private final TestTarget_B target;

    public TestComponent_B(TestTarget_B target) {
        this.target = target;
    }

    @EventHandler
    protected void onTestEvent(TestEvent_A event) {
        // TODO not called at the moment, EventDispatcher of parent target types not correctly linked yet
        LOGGER.info("TestEvent_A received.");
    }

    @EventHandler
    protected void onTest2Event(TestEvent_B event) {
        LOGGER.info("TestEvent_B received.");
    }

}
