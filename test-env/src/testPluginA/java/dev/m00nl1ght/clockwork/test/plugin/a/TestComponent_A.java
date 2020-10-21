package dev.m00nl1ght.clockwork.test.plugin.a;

import dev.m00nl1ght.clockwork.extension.annotations.EventHandler;
import dev.m00nl1ght.clockwork.test.TestInterface;
import dev.m00nl1ght.clockwork.test.TestTarget_A;
import dev.m00nl1ght.clockwork.test.event.SimpleTestEvent;
import dev.m00nl1ght.clockwork.util.Loggers;
import org.apache.logging.log4j.Logger;

public class TestComponent_A implements TestInterface {

    private static final Logger LOGGER = Loggers.getLogger("TestComponent_A");

    private final TestTarget_A target;

    public TestComponent_A(TestTarget_A target) {
        this.target = target;
    }

    @EventHandler
    private void onSimpleTestEvent(SimpleTestEvent event) {
        LOGGER.info("SimpleTestEvent received for " + target.getClass().getSimpleName() + ".");
    }

    @Override
    public void tick() {
        LOGGER.info("Ticked " + target.getClass().getSimpleName() + ".");
    }

}
