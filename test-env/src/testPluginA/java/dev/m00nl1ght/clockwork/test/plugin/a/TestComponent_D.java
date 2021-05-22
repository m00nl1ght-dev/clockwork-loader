package dev.m00nl1ght.clockwork.test.plugin.a;

import dev.m00nl1ght.clockwork.core.Component;
import dev.m00nl1ght.clockwork.extension.annotations.EventHandler;
import dev.m00nl1ght.clockwork.test.env.TestTarget_D;
import dev.m00nl1ght.clockwork.test.env.events.GenericTestEvent;
import dev.m00nl1ght.clockwork.test.env.events.SimpleTestEvent;

public class TestComponent_D extends Component<TestTarget_D> {

    TestComponent_D(TestTarget_D target) {
        super(target);
    }

    @EventHandler
    void onSimpleTestEvent(SimpleTestEvent event) {
        event.getTestContext().addMarker("TestComponent_D#onSimpleTestEvent");
    }

    @EventHandler
    void onGenericTestEvent(GenericTestEvent<String> event) {
        event.getTestContext().addMarker("TestComponent_D#onGenericTestEvent");
    }

}
