package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.EventType;
import dev.m00nl1ght.clockwork.event.EventHandler;
import dev.m00nl1ght.clockwork.test.event.PluginInitEvent;
import dev.m00nl1ght.clockwork.test.event.TestEvent_A;
import dev.m00nl1ght.clockwork.test.event.TestEvent_B;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestEnvPlugin {

    private static final Logger LOGGER = LogManager.getLogger();

    private final EventType<TestEvent_A, TestTarget_A> TEST_EVENT_A;
    private final EventType<TestEvent_B, TestTarget_B> TEST_EVENT_B;
    private final TestTarget_A TEST_TARGET_A;
    private final TestTarget_B TEST_TARGET_B;
    private final TestTarget_C TEST_TARGET_C;

    public TestEnvPlugin(ClockworkCore core) {
        TEST_TARGET_A = new TestTarget_A();
        TEST_TARGET_B = new TestTarget_B();
        TEST_TARGET_C = new TestTarget_C();
        TEST_EVENT_A = TestTarget_A.TARGET_TYPE.getEventType(TestEvent_A.class);
        TEST_EVENT_B = TestTarget_B.TARGET_TYPE.getEventType(TestEvent_B.class);
    }

    @EventHandler
    public void onInit(PluginInitEvent event) {
        LOGGER.info("Init event received.");
        TEST_EVENT_A.post(TEST_TARGET_A, new TestEvent_A());
        TEST_EVENT_A.post(TEST_TARGET_B, new TestEvent_A());
        TEST_EVENT_B.post(TEST_TARGET_B, new TestEvent_B());
    }

}
