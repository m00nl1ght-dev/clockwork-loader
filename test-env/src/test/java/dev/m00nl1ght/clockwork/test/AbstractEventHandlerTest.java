package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.impl.EventBusImpl;
import dev.m00nl1ght.clockwork.test.env.*;
import dev.m00nl1ght.clockwork.test.env.events.GenericTestEvent;
import dev.m00nl1ght.clockwork.test.env.events.SimpleTestEvent;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractEventHandlerTest extends ClockworkTest {

    protected TargetType<TestTarget_A> targetTypeA;
    protected TargetType<TestTarget_B> targetTypeB;
    protected TargetType<TestTarget_C> targetTypeC;
    protected TargetType<TestTarget_D> targetTypeD;

    @Override
    protected TestEnvironment buildEnvironment(ClockworkCore core) {
        final var env = super.buildEnvironment(core);
        targetTypeA = core.getTargetType(TestTarget_A.class).orElseThrow();
        targetTypeB = core.getTargetType(TestTarget_B.class).orElseThrow();
        targetTypeC = core.getTargetType(TestTarget_C.class).orElseThrow();
        targetTypeD = core.getTargetType(TestTarget_D.class).orElseThrow();
        final var nestedComp = targetTypeA.getOwnComponentType(TestTarget_C.class).orElseThrow();
        nestedComp.setFactory(TestTarget_A::getTestTargetC);
        return env;
    }

    protected abstract EventBusImpl eventBus();

    @Override
    protected void setupComplete() {
        // this is needed so that annotated static and nested handlers get registered, TODO some way around this?
        eventBus().getNestedEventDispatcher(SimpleTestEvent.class, TestTarget_C.class, TestTarget_A.class);
        eventBus().getNestedEventDispatcher(new TypeRef<GenericTestEvent<String>>(){}, TestTarget_C.class, TestTarget_A.class);
        eventBus().getStaticEventDispatcher(SimpleTestEvent.class, TestTarget_A.class);
        eventBus().getStaticEventDispatcher(new TypeRef<GenericTestEvent<String>>(){}, TestTarget_A.class);
    }

    @Test
    public void simpleEventOnTargetA() {
        final var dispatcher = eventBus().getEventDispatcher(SimpleTestEvent.class, TestTarget_A.class);
        final var testTargetA = new TestTarget_A(targetTypeA, new TestTarget_C(targetTypeC));
        final var event = new SimpleTestEvent();
        dispatcher.post(testTargetA, event);
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_A#onSimpleTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onSimpleTestEventForComponentA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onSimpleTestEventForTargetA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_C#onSimpleTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onSimpleTestEvent"));
    }

    @Test
    public void genericEventOnTargetA() {
        final var dispatcher = eventBus().getEventDispatcher(new TypeRef<GenericTestEvent<String>>(){}, TestTarget_A.class);
        final var testTargetA = new TestTarget_A(targetTypeA, new TestTarget_D(targetTypeD));
        final var event = new GenericTestEvent<>("dummy");
        dispatcher.post(testTargetA, event);
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_A#onGenericTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onGenericTestEventForComponentA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onGenericTestEventForTargetA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_C#onGenericTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_D#onGenericTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onGenericTestEvent"));
    }

    @Test
    public void simpleEventOnTargetB() {
        final var dispatcher = eventBus().getEventDispatcher(SimpleTestEvent.class, TestTarget_A.class);
        final var testTargetB = new TestTarget_B(targetTypeB, new TestTarget_C(targetTypeC));
        final var event = new SimpleTestEvent();
        dispatcher.post(testTargetB, event);
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_A#onSimpleTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_B#onSimpleTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onSimpleTestEventForComponentA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onSimpleTestEventForComponentB"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onSimpleTestEventForTargetA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onSimpleTestEventForTargetB"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_C#onSimpleTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onSimpleTestEvent"));
    }

    @Test
    public void genericEventOnTargetB() {
        final var dispatcher = eventBus().getEventDispatcher(new TypeRef<GenericTestEvent<String>>(){}, TestTarget_A.class);
        final var testTargetB = new TestTarget_B(targetTypeB, new TestTarget_D(targetTypeD));
        final var event = new GenericTestEvent<>("dummy");
        dispatcher.post(testTargetB, event);
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_A#onGenericTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_B#onGenericTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onGenericTestEventForComponentA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onGenericTestEventForComponentB"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onGenericTestEventForTargetA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onGenericTestEventForTargetB"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_C#onGenericTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_D#onGenericTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onGenericTestEvent"));
    }

}
