package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.event.EventHandler;
import dev.m00nl1ght.clockwork.event.EventType;
import dev.m00nl1ght.clockwork.test.event.PluginInitEvent;
import dev.m00nl1ght.clockwork.test.event.TestEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestEnvPlugin {

    private static final Logger LOGGER = LogManager.getLogger();

    private final EventType<TestEvent, TestComponentTarget> TEST_EVENT;
    private final TestComponentTarget TEST_TARGET_INSTANCE;

    public TestEnvPlugin(ClockworkCore core) {
        final var testTargetType = core.getTargetType(TestComponentTarget.class).orElseThrow();
        TEST_TARGET_INSTANCE = new TestComponentTarget(testTargetType);
        TEST_EVENT = testTargetType.getEventType(TestEvent.class);
    }

    @EventHandler
    public void onInit(PluginInitEvent event) {
        LOGGER.info("Init event received.");
        TEST_EVENT.post(TEST_TARGET_INSTANCE, new TestEvent());
    }

}
