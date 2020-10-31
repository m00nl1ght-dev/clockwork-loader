package dev.m00nl1ght.clockwork.test.env;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.events.EventBus;
import dev.m00nl1ght.clockwork.events.impl.ContextAwareEvent;

import java.io.File;
import java.util.Objects;

public class TestEnvironment {

    public static final File ENV_DIR = new File(".");
    public static final File PLUGINS_DIR = new File(ENV_DIR, "plugins");

    public static TestEnvironment get(ClockworkCore core) {
        final var testEnvComp = core.getComponentType(TestEnvironment.class, ClockworkCore.class).orElseThrow();
        return Objects.requireNonNull(testEnvComp.get(core));
    }

    private EventBus<ContextAwareEvent> testEventBus;

    public EventBus<ContextAwareEvent> getTestEventBus() {
        return testEventBus;
    }

    public void setTestEventBus(EventBus<ContextAwareEvent> testEventBus) {
        this.testEventBus = testEventBus;
    }

}
