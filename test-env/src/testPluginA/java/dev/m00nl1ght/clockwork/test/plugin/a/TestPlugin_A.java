package dev.m00nl1ght.clockwork.test.plugin.a;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.test.env.TestEnvironment;
import dev.m00nl1ght.clockwork.test.env.TestTarget_A;
import dev.m00nl1ght.clockwork.test.env.events.SimpleTestEvent;
import dev.m00nl1ght.clockwork.util.Loggers;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.apache.logging.log4j.Logger;

public class TestPlugin_A {

    private static final Logger LOGGER = Loggers.getLogger("TestPlugin_A");

    private final ClockworkCore core;

    public TestPlugin_A(ClockworkCore core) {
        this.core = core;
        final var eventBus = TestEnvironment.get(core).getTestEventBus();
        if (eventBus != null) {
            eventBus.addListener(SimpleTestEvent.class, TestTarget_A.class, TestComponent_A.class, TestComponent_A::onSimpleTestEvent);
            eventBus.addListener(new TypeRef<>(){}, TestTarget_A.class, TestComponent_A.class, TestComponent_A::onGenericTestEvent);
            eventBus.addListener(SimpleTestEvent.class, TestTarget_A.class, TestComponent_B.class, TestComponent_B::onSimpleTestEvent);
            eventBus.addListener(new TypeRef<>(){}, TestTarget_A.class, TestComponent_B.class, TestComponent_B::onGenericTestEvent);
        }
    }

}
