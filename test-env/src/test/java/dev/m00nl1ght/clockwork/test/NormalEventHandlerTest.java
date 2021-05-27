package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.event.impl.bus.EventBusImpl;
import dev.m00nl1ght.clockwork.test.env.TestEnvironment;
import dev.m00nl1ght.clockwork.test.env.TestTarget_A;

public class NormalEventHandlerTest extends AbstractEventHandlerTest {

    private EventBusImpl eventBus;

    @Override
    protected TestEnvironment buildEnvironment(ClockworkCore core) {
        final var env = super.buildEnvironment(core);
        eventBus = new EventBusImpl();
        eventBus.addForwardingPolicy(targetTypeA, targetTypeC, TestTarget_A::getTestTargetC);
        env.setTestEventBus(eventBus);
        return env;
    }

    @Override
    protected EventBusImpl eventBus() {
        return eventBus;
    }

}
