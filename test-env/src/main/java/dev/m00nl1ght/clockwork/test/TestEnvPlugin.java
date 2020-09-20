package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.extension.annotations.EventHandler;
import dev.m00nl1ght.clockwork.test.event.GenericTestEvent;
import dev.m00nl1ght.clockwork.test.event.PluginInitEvent;
import dev.m00nl1ght.clockwork.test.event.SimpleTestEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestEnvPlugin {

    private static final Logger LOGGER = LogManager.getLogger();

    private TestTarget_A TEST_TARGET_A;
    private TestTarget_B TEST_TARGET_B;
    private TestTarget_C TEST_TARGET_C;

    @EventHandler
    public void onInit(PluginInitEvent event) {
        LOGGER.info("Init event received.");
        TEST_TARGET_A = new TestTarget_A();
        TEST_TARGET_B = new TestTarget_B();
        TEST_TARGET_C = new TestTarget_C();
        LOGGER.info("Posting SimpleTestEvent to TEST_TARGET_A.");
        SimpleTestEvent.TYPE.post(TEST_TARGET_A, new SimpleTestEvent());
        LOGGER.info("Posting SimpleTestEvent to TEST_TARGET_B.");
        SimpleTestEvent.TYPE.post(TEST_TARGET_B, new SimpleTestEvent());
        LOGGER.info("Posting GenericTestEvents to TEST_TARGET_B.");
        GenericTestEvent.TYPE_STRING.post(TEST_TARGET_B, new GenericTestEvent<>());
        GenericTestEvent.TYPE_RAW.post(TEST_TARGET_B, new GenericTestEvent());
        LOGGER.info("Applying TestInterface to TEST_TARGET_A.");
        TestInterface.TYPE.apply(TEST_TARGET_A, TestInterface::tick);
        LOGGER.info("Applying TestInterface to TEST_TARGET_B.");
        TestInterface.TYPE.apply(TEST_TARGET_B, TestInterface::tick);
    }

}
