package dev.m00nl1ght.clockwork.test.env;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.events.EventBus;
import dev.m00nl1ght.clockwork.events.impl.ContextAwareEvent;

import java.io.File;
import java.util.Objects;

public class TestEnvironment {

    public static final File ENV_DIR = new File(".").getAbsoluteFile();
    public static final File PLUGINS_DIR = new File(ENV_DIR, "plugins");
    public static final File PLUGIN_DATA_DIR = new File(ENV_DIR, "plugin-data");
    public static final File PLUGIN_SHARED_DIR = new File(ENV_DIR, "shared-data");
    public static final File PLUGIN_PROTECTED_DIR_A = new File(ENV_DIR, "protected-data-a");
    public static final File PLUGIN_PROTECTED_DIR_B = new File(ENV_DIR, "protected-data-b");

    public static TestEnvironment get(ClockworkCore core) {
        final var testEnvComp = core.getComponentType(TestEnvironment.class, ClockworkCore.class).orElseThrow();
        return Objects.requireNonNull(testEnvComp.get(core));
    }

    private EventBus<ContextAwareEvent> testEventBus;

    public TestEnvironment() {
        ENV_DIR.mkdirs();
        PLUGINS_DIR.mkdirs();
        PLUGIN_DATA_DIR.mkdirs();
        PLUGIN_SHARED_DIR.mkdirs();
        PLUGIN_PROTECTED_DIR_A.mkdirs();
        PLUGIN_PROTECTED_DIR_B.mkdirs();
    }

    public EventBus<ContextAwareEvent> getTestEventBus() {
        return testEventBus;
    }

    public void setTestEventBus(EventBus<ContextAwareEvent> testEventBus) {
        this.testEventBus = testEventBus;
    }

}
