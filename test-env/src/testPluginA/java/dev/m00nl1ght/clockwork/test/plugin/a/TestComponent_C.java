package dev.m00nl1ght.clockwork.test.plugin.a;

import dev.m00nl1ght.clockwork.core.Component;
import dev.m00nl1ght.clockwork.extension.annotations.EventHandler;
import dev.m00nl1ght.clockwork.test.env.TestTarget_C;
import dev.m00nl1ght.clockwork.test.env.events.GenericTestEvent;
import dev.m00nl1ght.clockwork.test.env.events.SimpleTestEvent;

public class TestComponent_C extends Component<TestTarget_C> {

    TestComponent_C(TestTarget_C target) {
        super(target);
    }

    @EventHandler
    void onSimpleTestEvent(SimpleTestEvent event) {
        event.getTestContext().addMarker("TestComponent_C#onSimpleTestEvent");
    }

    @EventHandler
    void onGenericTestEvent(GenericTestEvent<String> event) {
        event.getTestContext().addMarker("TestComponent_C#onGenericTestEvent");
    }

}
