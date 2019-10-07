package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTargetType;

public class TestLauncher {

    private static final ClockworkCore core = new ClockworkCore();

    public static void main(String... args) {
        core.registerLocator(new TestPluginLoader());
        core.registerComponentTarget(new ComponentTargetType<>("test:holder", TestComponentTarget.class));
        core.loadPlugins();
    }

}
