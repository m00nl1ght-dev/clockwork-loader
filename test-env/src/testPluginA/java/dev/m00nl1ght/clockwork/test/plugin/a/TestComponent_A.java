package dev.m00nl1ght.clockwork.test.plugin.a;

import dev.m00nl1ght.clockwork.extension.annotations.EventHandler;
import dev.m00nl1ght.clockwork.test.env.TestTarget_A;
import dev.m00nl1ght.clockwork.test.env.events.GenericTestEvent;
import dev.m00nl1ght.clockwork.test.env.events.SimpleTestEvent;

public class TestComponent_A {

    TestComponent_A(TestTarget_A target) {

    }

    @EventHandler
    void onSimpleTestEvent(SimpleTestEvent event) {
        event.setHandledBy("TestComponent_A#onSimpleTestEvent");
    }

    @EventHandler
    void onGenericTestEvent(GenericTestEvent<String> event) {
        event.setHandledBy("TestComponent_A#onGenericTestEvent");
    }

}
