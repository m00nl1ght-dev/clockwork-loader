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
        SimpleTestEvent.TYPE.post(TEST_TARGET_A, new SimpleTestEvent());
        SimpleTestEvent.TYPE.post(TEST_TARGET_B, new SimpleTestEvent());
        GenericTestEvent.TYPE_STRING.post(TEST_TARGET_B, new GenericTestEvent<>());
        GenericTestEvent.TYPE_RAW.post(TEST_TARGET_B, new GenericTestEvent());
        TestInterface.TYPE.apply(TEST_TARGET_A, TestInterface::tick);
        TestInterface.TYPE.apply(TEST_TARGET_B, TestInterface::tick);
        LOGGER.info("TEST_TARGET_A CI count:" + TestInterface.TYPE.stream(TEST_TARGET_A).count());
        LOGGER.info("TEST_TARGET_B CI count:" + TestInterface.TYPE.stream(TEST_TARGET_B).count());
    }

}
