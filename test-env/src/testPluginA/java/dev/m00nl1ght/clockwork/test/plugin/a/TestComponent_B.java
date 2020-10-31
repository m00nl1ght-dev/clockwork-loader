package dev.m00nl1ght.clockwork.test.plugin.a;

import dev.m00nl1ght.clockwork.extension.annotations.EventHandler;
import dev.m00nl1ght.clockwork.test.env.TestContext;
import dev.m00nl1ght.clockwork.test.env.TestInterface;
import dev.m00nl1ght.clockwork.test.env.TestTarget_B;
import dev.m00nl1ght.clockwork.test.env.events.GenericTestEvent;
import dev.m00nl1ght.clockwork.test.env.events.SimpleTestEvent;

public class TestComponent_B implements TestInterface {

    TestComponent_B(TestTarget_B target) {

    }

    @EventHandler
    void onSimpleTestEvent(SimpleTestEvent event) {
        event.getTestContext().addMarker("TestComponent_B#onSimpleTestEvent");
    }

    @EventHandler
    void onGenericTestEvent(GenericTestEvent<String> event) {
        event.getTestContext().addMarker("TestComponent_B#onGenericTestEvent");
    }

    @Override
    public void applyTestInterface(TestContext testContext) {
        testContext.addMarker("TestComponent_B#applyTestInterface");
    }

}
