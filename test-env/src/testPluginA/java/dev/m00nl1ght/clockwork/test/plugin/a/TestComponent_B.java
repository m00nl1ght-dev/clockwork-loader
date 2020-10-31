package dev.m00nl1ght.clockwork.test.plugin.a;

import dev.m00nl1ght.clockwork.test.env.TestTarget_B;
import dev.m00nl1ght.clockwork.test.env.events.GenericTestEvent;
import dev.m00nl1ght.clockwork.test.env.events.SimpleTestEvent;

public class TestComponent_B {

    TestComponent_B(TestTarget_B target) {

    }

    void onSimpleTestEvent(SimpleTestEvent event) {
        event.setHandledBy("TestComponent_B#onSimpleTestEvent");
    }

    void onGenericTestEvent(GenericTestEvent<String> event) {
        event.setHandledBy("TestComponent_B#onGenericTestEvent");
    }

}
