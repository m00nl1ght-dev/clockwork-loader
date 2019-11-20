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
        final var testTargetTypeA = core.getTargetType(TestTarget_A.class).orElseThrow();
        final var testTargetTypeB = core.getTargetType(TestTarget_B.class).orElseThrow();
        final var testTargetTypeC = core.getTargetType(TestTarget_C.class).orElseThrow();
        TEST_TARGET_A = new TestTarget_A(testTargetTypeA);
        TEST_TARGET_B = new TestTarget_B(testTargetTypeB);
        TEST_TARGET_C = new TestTarget_C(testTargetTypeC);
        TEST_EVENT_A = testTargetTypeA.getEventType(TestEvent_A.class);
        TEST_EVENT_B = testTargetTypeB.getEventType(TestEvent_B.class);
    }

    @EventHandler
    public void onInit(PluginInitEvent event) {
        LOGGER.info("Init event received.");
        TEST_EVENT_A.post(TEST_TARGET_A, new TestEvent_A());
        TEST_EVENT_A.post(TEST_TARGET_B, new TestEvent_A());
        TEST_EVENT_B.post(TEST_TARGET_B, new TestEvent_B());
    }

}
