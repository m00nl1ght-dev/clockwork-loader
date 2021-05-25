package dev.m00nl1ght.clockwork.test.env.events;

import dev.m00nl1ght.clockwork.event.impl.event.EventWithContext;
import dev.m00nl1ght.clockwork.test.env.TestContext;

public abstract class TestEvent extends EventWithContext {

    private final TestContext testContext = new TestContext();

    public TestContext getTestContext() {
        return testContext;
    }

}
