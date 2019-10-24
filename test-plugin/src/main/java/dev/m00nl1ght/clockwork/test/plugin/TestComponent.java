package dev.m00nl1ght.clockwork.test.plugin;

import dev.m00nl1ght.clockwork.event.EventHandler;
import dev.m00nl1ght.clockwork.test.TestComponentTarget;
import dev.m00nl1ght.clockwork.test.event.TestEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestComponent {

    private static final Logger LOGGER = LogManager.getLogger();

    private final TestComponentTarget target;

    public TestComponent(TestComponentTarget target) {
        this.target = target;
    }

    @EventHandler
    protected void onTestEvent(TestEvent event) {
        LOGGER.info("TestEvent received on " + this.toString());
    }

}
