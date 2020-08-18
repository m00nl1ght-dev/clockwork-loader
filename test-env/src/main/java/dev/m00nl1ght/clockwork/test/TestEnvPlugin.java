package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.extension.eventhandler.EventHandler;
import dev.m00nl1ght.clockwork.test.event.PluginInitEvent;
import dev.m00nl1ght.clockwork.test.event.TestEvent_A;
import dev.m00nl1ght.clockwork.test.event.TestEvent_B;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestEnvPlugin {

    private static final Logger LOGGER = LogManager.getLogger();

    private TestTarget_A TEST_TARGET_A;
    private TestTarget_B TEST_TARGET_B;
    private TestTarget_C TEST_TARGET_C;

    public TestEnvPlugin(ClockworkCore core) {

    }

    @EventHandler
    public void onInit(PluginInitEvent event) {
        LOGGER.info("Init event received.");
        TEST_TARGET_A = new TestTarget_A();
        TEST_TARGET_B = new TestTarget_B();
        TEST_TARGET_C = new TestTarget_C();
        TestEvent_A.TYPE.post(TEST_TARGET_A, new TestEvent_A());
        TestEvent_A.TYPE.post(TEST_TARGET_B, new TestEvent_A());
        TestEvent_B.TYPE.post(TEST_TARGET_B, new TestEvent_B());
        TestInterface.TYPE.apply(TEST_TARGET_A, TestInterface::tick);
        TestInterface.TYPE.apply(TEST_TARGET_B, TestInterface::tick);
    }

}
