package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.component.TargetType;
import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.event.impl.bus.EventBusImpl;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.test.env.*;
import dev.m00nl1ght.clockwork.test.env.events.GenericTestEvent;
import dev.m00nl1ght.clockwork.test.env.events.SimpleTestEvent;
import dev.m00nl1ght.clockwork.utils.profiler.ProfilerGroup;
import dev.m00nl1ght.clockwork.utils.profiler.ProfilerUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractEventHandlerTest extends ClockworkTest {

    protected TargetType<TestTarget_A> targetTypeA;
    protected TargetType<TestTarget_B> targetTypeB;
    protected TargetType<TestTarget_C> targetTypeC;
    protected TargetType<TestTarget_D> targetTypeD;

    protected ProfilerGroup profilerGroup;

    @Override
    protected TestEnvironment buildEnvironment(ClockworkCore core) {
        final var env = super.buildEnvironment(core);
        targetTypeA = core.getTargetTypeOrThrow(TestTarget_A.class);
        targetTypeB = core.getTargetTypeOrThrow(TestTarget_B.class);
        targetTypeC = core.getTargetTypeOrThrow(TestTarget_C.class);
        targetTypeD = core.getTargetTypeOrThrow(TestTarget_D.class);
        return env;
    }

    @Override
    protected void setupComplete(ClockworkLoader loader) {
        profilerGroup = eventBus().attachDefaultProfiler();
        super.setupComplete(loader);
    }

    protected abstract EventBusImpl eventBus();

    @Test
    public void simpleEventOnTargetA() {
        final var dispatcher = eventBus().getEventDispatcher(SimpleTestEvent.class, targetTypeA);
        final var testTargetA = new TestTarget_A(targetTypeA, new TestTarget_C(targetTypeC));
        final var event = new SimpleTestEvent();
        dispatcher.post(testTargetA, event);
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_A#onSimpleTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onSimpleTestEventForComponentA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onSimpleTestEventForTargetA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_C#onSimpleTestEvent"));
    }

    @Test
    public void genericEventOnTargetA() {
        final var dispatcher = eventBus().getEventDispatcher(GenericTestEvent.STRING_TYPE, targetTypeA);
        final var testTargetA = new TestTarget_A(targetTypeA, new TestTarget_D(targetTypeD));
        final var event = new GenericTestEvent<>("dummy");
        dispatcher.post(testTargetA, event);
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_A#onGenericTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onGenericTestEventForComponentA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestPlugin_A#onGenericTestEventForTargetA"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_C#onGenericTestEvent"));
        assertTrue(event.getTestContext().isMarkerPresent("TestComponent_D#onGenericTestEvent"));
    }

    @Test
    public void simpleEventOnTargetB() {
        final var dispatcher = eventBus().getEventDispatcher(SimpleTestEvent.class, targetTypeA);
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
    }

    @Test
    public void genericEventOnTargetB() {
        final var dispatcher = eventBus().getEventDispatcher(GenericTestEvent.STRING_TYPE, targetTypeA);
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
    }

    @AfterAll
    public void profilerResults() {
        System.out.println(ProfilerUtils.printProfilerInfo(profilerGroup));
    }

}
