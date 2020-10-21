package dev.m00nl1ght.clockwork.test.plugin.a;

import dev.m00nl1ght.clockwork.extension.annotations.EventHandler;
import dev.m00nl1ght.clockwork.test.TestInterface;
import dev.m00nl1ght.clockwork.test.TestTarget_B;
import dev.m00nl1ght.clockwork.test.event.GenericTestEvent;
import dev.m00nl1ght.clockwork.test.event.SimpleTestEvent;
import dev.m00nl1ght.clockwork.util.Loggers;
import org.apache.logging.log4j.Logger;

public class TestComponent_B implements TestInterface {

    private static final Logger LOGGER = Loggers.getLogger("TestComponent_B");

    private final TestTarget_B target;

    public TestComponent_B(TestTarget_B target) {
        this.target = target;
    }

    @EventHandler
    private void onSimpleTestEvent(SimpleTestEvent event) {
        LOGGER.info("SimpleTestEvent received for " + target.getClass().getSimpleName() + ".");
    }

    @EventHandler
    private void onGenericTestEvent(GenericTestEvent<String> event) {
        LOGGER.info("GenericTestEvent<String> received for " + target.getClass().getSimpleName() + ".");
    }

    @EventHandler
    private void onGenericTestEventRaw(GenericTestEvent event) {
        LOGGER.info("Raw GenericTestEvent received for " + target.getClass().getSimpleName() + ".");
    }

    @Override
    public void tick() {
        LOGGER.info("Ticked " + target.getClass().getSimpleName() + ".");
    }

}
