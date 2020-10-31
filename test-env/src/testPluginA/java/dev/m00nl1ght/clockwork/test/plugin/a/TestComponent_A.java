package dev.m00nl1ght.clockwork.test.plugin.a;

import dev.m00nl1ght.clockwork.extension.annotations.EventHandler;
import dev.m00nl1ght.clockwork.test.env.TestContext;
import dev.m00nl1ght.clockwork.test.env.TestInterface;
import dev.m00nl1ght.clockwork.test.env.TestTarget_A;
import dev.m00nl1ght.clockwork.test.env.events.GenericTestEvent;
import dev.m00nl1ght.clockwork.test.env.events.SimpleTestEvent;

public class TestComponent_A implements TestInterface {

    TestComponent_A(TestTarget_A target) {

    }

    @EventHandler
    void onSimpleTestEvent(SimpleTestEvent event) {
        event.getTestContext().addMarker("TestComponent_A#onSimpleTestEvent");
    }

    @EventHandler
    void onGenericTestEvent(GenericTestEvent<String> event) {
        event.getTestContext().addMarker("TestComponent_A#onGenericTestEvent");
    }

    @Override
    public void applyTestInterface(TestContext testContext) {
        testContext.addMarker("TestComponent_A#applyTestInterface");
    }

}
