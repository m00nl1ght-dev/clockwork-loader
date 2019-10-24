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
        // will no longer be needed once @EventHandler is hooked up
        final var coreTarget = core.getTargetType(ClockworkCore.class).orElseThrow();
        final var self = core.getComponentType(TestEnvPlugin.class, ClockworkCore.class).orElseThrow();
        coreTarget.getEventType(PluginInitEvent.class).registerListener(self, TestEnvPlugin::onInit);
        // ^^ / ^^

        final var testTargetType = core.getTargetType(TestComponentTarget.class).orElseThrow();
        TEST_TARGET_INSTANCE = new TestComponentTarget(testTargetType);
        TEST_EVENT = testTargetType.registerEvent(TestEvent.class);
    }

    @EventHandler
    private void onInit(PluginInitEvent event) {
        LOGGER.info("Firing test event");
        TEST_EVENT.post(TEST_TARGET_INSTANCE, new TestEvent());
    }

}
