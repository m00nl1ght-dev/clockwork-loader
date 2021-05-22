package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.events.impl.AdvancedEventBusImpl;
import dev.m00nl1ght.clockwork.events.impl.EventBusImpl;
import dev.m00nl1ght.clockwork.test.env.TestEnvironment;

public class NormalEventHandlerTest extends AbstractEventHandlerTest {

    private EventBusImpl eventBus;

    @Override
    protected TestEnvironment buildEnvironment(ClockworkCore core) {
        final var env = super.buildEnvironment(core);
        eventBus = new AdvancedEventBusImpl();
        env.setTestEventBus(eventBus);
        return env;
    }

    @Override
    protected EventBusImpl eventBus() {
        return eventBus;
    }

}
