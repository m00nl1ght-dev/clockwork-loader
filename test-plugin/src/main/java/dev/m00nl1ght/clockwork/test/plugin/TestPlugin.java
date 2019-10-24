package dev.m00nl1ght.clockwork.test.plugin;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.event.EventHandler;
import dev.m00nl1ght.clockwork.test.TestComponentTarget;
import dev.m00nl1ght.clockwork.test.event.PluginInitEvent;
import dev.m00nl1ght.clockwork.test.event.TestEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestPlugin {

    private static final Logger LOGGER = LogManager.getLogger();

    public TestPlugin(ClockworkCore core) {
        // will no longer be needed once @EventHandler is hooked up
        final var coreTarget = core.getTargetType(ClockworkCore.class).orElseThrow();
        final var self = core.getComponentType(TestPlugin.class, ClockworkCore.class).orElseThrow();
        coreTarget.getEventType(PluginInitEvent.class).registerListener(self, TestPlugin::onInit);
        final var testTarget = core.getTargetType(TestComponentTarget.class).orElseThrow();
        final var testComponent = core.getComponentType(TestComponent.class, TestComponentTarget.class).orElseThrow();
        testTarget.getEventType(TestEvent.class).registerListener(testComponent, TestComponent::onTestEvent);
        // ^^ / ^^
    }

    @EventHandler
    public void onInit(PluginInitEvent event) {
        LOGGER.info("Init event fired.");
    }

}
