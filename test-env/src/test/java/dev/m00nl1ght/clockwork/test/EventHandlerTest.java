package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.impl.EventBusImpl;
import dev.m00nl1ght.clockwork.test.env.TestEnvironment;
import dev.m00nl1ght.clockwork.test.env.TestTarget_A;
import dev.m00nl1ght.clockwork.test.env.TestTarget_B;
import dev.m00nl1ght.clockwork.test.env.events.GenericTestEvent;
import dev.m00nl1ght.clockwork.test.env.events.SimpleTestEvent;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventHandlerTest extends ClockworkTest {

    private EventBusImpl eventBus;
    private TargetType<TestTarget_A> targetTypeA;
    private TargetType<TestTarget_B> targetTypeB;

    @Override
    protected TestEnvironment buildEnvironment(ClockworkCore core) {
        final var env = super.buildEnvironment(core);
        targetTypeA = core.getTargetType(TestTarget_A.class).orElseThrow();
        targetTypeB = core.getTargetType(TestTarget_B.class).orElseThrow();
        eventBus = new EventBusImpl(core);
        env.setTestEventBus(eventBus);
        return env;
    }

    @Test
    public void simpleEventOnTargetA() {
        final var dispatcher = eventBus.getEventDispatcher(SimpleTestEvent.class, TestTarget_A.class);
        final var testTargetA = new TestTarget_A(targetTypeA);
        final var event = new SimpleTestEvent();
        dispatcher.post(testTargetA, event);
        assertTrue(event.wasHandledBy("TestComponent_A#onSimpleTestEvent"));
    }

    @Test
    public void genericEventOnTargetA() {
        final var dispatcher = eventBus.getEventDispatcher(new TypeRef<GenericTestEvent<String>>(){}, TestTarget_A.class);
        final var testTargetA = new TestTarget_A(targetTypeA);
        final var event = new GenericTestEvent<>("dummy");
        dispatcher.post(testTargetA, event);
        assertTrue(event.wasHandledBy("TestComponent_A#onGenericTestEvent"));
    }

    @Test
    public void simpleEventOnTargetB() {
        final var dispatcher = eventBus.getEventDispatcher(SimpleTestEvent.class, TestTarget_A.class);
        final var testTargetB = new TestTarget_B(targetTypeB);
        final var event = new SimpleTestEvent();
        dispatcher.post(testTargetB, event);
        assertTrue(event.wasHandledBy("TestComponent_A#onSimpleTestEvent"));
        assertTrue(event.wasHandledBy("TestComponent_B#onSimpleTestEvent"));
    }

    @Test
    public void genericEventOnTargetB() {
        final var dispatcher = eventBus.getEventDispatcher(new TypeRef<GenericTestEvent<String>>(){}, TestTarget_A.class);
        final var testTargetB = new TestTarget_B(targetTypeB);
        final var event = new GenericTestEvent<>("dummy");
        dispatcher.post(testTargetB, event);
        assertTrue(event.wasHandledBy("TestComponent_A#onGenericTestEvent"));
        assertTrue(event.wasHandledBy("TestComponent_B#onGenericTestEvent"));
    }

}
