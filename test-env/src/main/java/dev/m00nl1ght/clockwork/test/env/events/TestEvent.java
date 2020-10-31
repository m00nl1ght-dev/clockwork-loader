package dev.m00nl1ght.clockwork.test.env.events;

import dev.m00nl1ght.clockwork.events.impl.ContextAwareEvent;
import dev.m00nl1ght.clockwork.test.env.TestContext;

public abstract class TestEvent extends ContextAwareEvent {

    private final TestContext testContext = new TestContext();

    public TestContext getTestContext() {
        return testContext;
    }

}
