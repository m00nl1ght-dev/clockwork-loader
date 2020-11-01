package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.impl.EventBusImpl;
import dev.m00nl1ght.clockwork.test.env.TestEnvironment;
import dev.m00nl1ght.clockwork.test.env.TestTarget_A;
import dev.m00nl1ght.clockwork.test.env.TestTarget_B;
import dev.m00nl1ght.clockwork.test.env.TestTarget_C;
import dev.m00nl1ght.clockwork.test.env.events.GenericTestEvent;
import dev.m00nl1ght.clockwork.test.env.events.SimpleTestEvent;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractEventHandlerTest extends ClockworkTest {

    protected TargetType<TestTarget_A> targetTypeA;
    protected TargetType<TestTarget_B> targetTypeB;
    protected TargetType<TestTarget_C> targetTypeC;

    @Override
    protected TestEnvironment buildEnvironment(ClockworkCore core) {
        final var env = super.buildEnvironment(core);
        targetTypeA = core.getTargetType(TestTarget_A.class).orElseThrow();
        targetTypeB = core.getTargetType(TestTarget_B.class).orElseThrow();
        targetTypeC = core.getTargetType(TestTarget_C.class).orElseThrow();
        final var nestedComp = targetTypeA.getComponentType(TestTarget_C.class).orElseThrow();
        nestedComp.setFactory(t -> new TestTarget_C(targetTypeC));
        return env;
    }

    protected abstract EventBusImpl eventBus();

    @Override
    protected void setupComplete() {
        // this is needed so that annotated nested handlers get registered, TODO some way around this?
        eventBus().getNestedEventDispatcher(SimpleTestEvent.class, TestTarget_C.class, TestTarget_A.class);
        eventBus().getNestedEventDispatcher(new TypeRef<GenericTestEvent<String>>(){}, TestTarget_C.class, TestTarget_A.class);
    }

    @Test
    public void simpleEventOnTargetA() {
        final var dispatcher = eventBus().getEventDispatcher(SimpleTestEvent.class, TestTarget_A.class);
        final var testTargetA = new TestTarget_A(targetTypeA);
        final var event = new SimpleTestEvent();
        dispatcher.post(testTargetA, event);
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_A#onSimpleTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onSimpleTestEventForComponentA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onSimpleTestEventForTargetA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_C#onSimpleTestEvent"));
    }

    @Test
    public void genericEventOnTargetA() {
        final var dispatcher = eventBus().getEventDispatcher(new TypeRef<GenericTestEvent<String>>(){}, TestTarget_A.class);
        final var testTargetA = new TestTarget_A(targetTypeA);
        final var event = new GenericTestEvent<>("dummy");
        dispatcher.post(testTargetA, event);
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_A#onGenericTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onGenericTestEventForComponentA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onGenericTestEventForTargetA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_C#onGenericTestEvent"));
    }

    @Test
    public void simpleEventOnTargetB() {
        final var dispatcher = eventBus().getEventDispatcher(SimpleTestEvent.class, TestTarget_A.class);
        final var testTargetB = new TestTarget_B(targetTypeB);
        final var event = new SimpleTestEvent();
        dispatcher.post(testTargetB, event);
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_A#onSimpleTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_B#onSimpleTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onSimpleTestEventForComponentA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onSimpleTestEventForComponentB"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onSimpleTestEventForTargetA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onSimpleTestEventForTargetB"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_C#onSimpleTestEvent"));
    }

    @Test
    public void genericEventOnTargetB() {
        final var dispatcher = eventBus().getEventDispatcher(new TypeRef<GenericTestEvent<String>>(){}, TestTarget_A.class);
        final var testTargetB = new TestTarget_B(targetTypeB);
        final var event = new GenericTestEvent<>("dummy");
        dispatcher.post(testTargetB, event);
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_A#onGenericTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_B#onGenericTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onGenericTestEventForComponentA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onGenericTestEventForComponentB"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onGenericTestEventForTargetA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onGenericTestEventForTargetB"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_C#onGenericTestEvent"));
    }

    // TODO tests for static event handlers

}
