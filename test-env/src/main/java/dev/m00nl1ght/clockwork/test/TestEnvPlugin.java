package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.event.EventHandler;
import dev.m00nl1ght.clockwork.event.EventType;
import dev.m00nl1ght.clockwork.test.event.PluginInitEvent;
import dev.m00nl1ght.clockwork.test.event.TestEvent;
import dev.m00nl1ght.clockwork.test.event.TestEvent2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestEnvPlugin {

    private static final Logger LOGGER = LogManager.getLogger();

    private final EventType<TestEvent, TestComponentTarget> TEST_EVENT;
    private final EventType<TestEvent, SubclassTestComponentTarget> TEST_EVENT_SUB;
    private final EventType<TestEvent2, SubclassTestComponentTarget> TEST_EVENT2;
    private final TestComponentTarget TEST_TARGET_INSTANCE;
    private final SubclassTestComponentTarget TEST2_TARGET_INSTANCE;

    public TestEnvPlugin(ClockworkCore core) {
        final var testTargetType = core.getTargetType(TestComponentTarget.class).orElseThrow();
        TEST_TARGET_INSTANCE = new TestComponentTarget(testTargetType);
        final var test2TargetType = core.getTargetType(SubclassTestComponentTarget.class).orElseThrow();
        TEST2_TARGET_INSTANCE = new SubclassTestComponentTarget(test2TargetType);
        TEST_EVENT = testTargetType.getEventType(TestEvent.class);
        TEST_EVENT_SUB = test2TargetType.getEventType(TestEvent.class);
        TEST_EVENT2 = test2TargetType.getEventType(TestEvent2.class);
    }

    @EventHandler
    public void onInit(PluginInitEvent event) {
        LOGGER.info("Init event received.");
        TEST_EVENT.post(TEST_TARGET_INSTANCE, new TestEvent());
        //TEST_EVENT2.post(TEST2_TARGET_INSTANCE, new TestEvent2()); TODO find way
    }

}
