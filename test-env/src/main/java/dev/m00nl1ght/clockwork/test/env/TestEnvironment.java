package dev.m00nl1ght.clockwork.test.env;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.MainComponent;
import dev.m00nl1ght.clockwork.events.impl.EventBusImpl;

import java.io.File;
import java.util.Objects;

public class TestEnvironment extends MainComponent {

    public static final File ENV_DIR = new File(".").getAbsoluteFile();
    public static final File PLUGINS_DIR = new File(ENV_DIR, "plugins");
    public static final File PLUGIN_DATA_DIR = new File(ENV_DIR, "plugin-data");
    public static final File PLUGIN_SHARED_DIR = new File(ENV_DIR, "shared-data");
    public static final File PLUGIN_PROTECTED_DIR_A = new File(ENV_DIR, "protected-data-a");
    public static final File PLUGIN_PROTECTED_DIR_B = new File(ENV_DIR, "protected-data-b");

    public static TestEnvironment get(ClockworkCore core) {
        final var testEnvComp = core.getComponentTypeOrThrow(TestEnvironment.class, ClockworkCore.class);
        return Objects.requireNonNull(testEnvComp.get(core));
    }

    private EventBusImpl testEventBus;

    public TestEnvironment(ClockworkCore core) {
        super(core);
        ENV_DIR.mkdirs();
        PLUGINS_DIR.mkdirs();
        PLUGIN_DATA_DIR.mkdirs();
        PLUGIN_SHARED_DIR.mkdirs();
        PLUGIN_PROTECTED_DIR_A.mkdirs();
        PLUGIN_PROTECTED_DIR_B.mkdirs();
    }

    public EventBusImpl getTestEventBus() {
        return testEventBus;
    }

    public void setTestEventBus(EventBusImpl testEventBus) {
        this.testEventBus = testEventBus;
    }

}
