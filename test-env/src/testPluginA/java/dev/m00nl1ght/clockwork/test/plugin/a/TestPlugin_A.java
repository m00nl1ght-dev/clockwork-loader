package dev.m00nl1ght.clockwork.test.plugin.a;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.extension.annotations.EventHandler;
import dev.m00nl1ght.clockwork.test.env.TestEnvironment;
import dev.m00nl1ght.clockwork.test.env.TestTarget_A;
import dev.m00nl1ght.clockwork.test.env.TestTarget_B;
import dev.m00nl1ght.clockwork.test.env.events.GenericTestEvent;
import dev.m00nl1ght.clockwork.test.env.events.SimpleTestEvent;
import dev.m00nl1ght.clockwork.util.TypeRef;

public class TestPlugin_A {

    private final ClockworkCore core;

    public TestPlugin_A(ClockworkCore core) {
        this.core = core;
        final var eventBus = TestEnvironment.get(core).getTestEventBus();
        if (eventBus != null) {

            final var targetTypeA = core.getTargetType(TestTarget_A.class).orElseThrow();
            final var targetTypeB = core.getTargetType(TestTarget_B.class).orElseThrow();
            final var componentTypeA = core.getComponentType(TestComponent_A.class, TestTarget_A.class).orElseThrow();
            final var componentTypeB = core.getComponentType(TestComponent_B.class, TestTarget_B.class).orElseThrow();

            final var simpleEventDispatcher = eventBus.getEventDispatcher(SimpleTestEvent.class, TestTarget_A.class);

            // SimpleTestEvent -> Component instance handlers
            simpleEventDispatcher.addListener(componentTypeA, TestComponent_A::onSimpleTestEvent);
            simpleEventDispatcher.addListener(componentTypeB, TestComponent_B::onSimpleTestEvent);

            // SimpleTestEvent -> Static handlers (with component param)
            simpleEventDispatcher.addListener(componentTypeA, TestPlugin_A::onSimpleTestEventForComponentA);
            simpleEventDispatcher.addListener(componentTypeB, TestPlugin_A::onSimpleTestEventForComponentB);

            // SimpleTestEvent -> Static handlers (with identity param)
            simpleEventDispatcher.addListener(targetTypeA, TestPlugin_A::onSimpleTestEventForTargetA);
            simpleEventDispatcher.addListener(targetTypeB, TestPlugin_A::onSimpleTestEventForTargetB);

            final var genericEventDispatcher = eventBus.getEventDispatcher(new TypeRef<GenericTestEvent<String>>(){}, TestTarget_A.class);

            // GenericTestEvent<String> -> Component instance handlers
            genericEventDispatcher.addListener(componentTypeA, TestComponent_A::onGenericTestEvent);
            genericEventDispatcher.addListener(componentTypeB, TestComponent_B::onGenericTestEvent);

            // GenericTestEvent<String> -> Static handlers (with component param)
            genericEventDispatcher.addListener(componentTypeA, TestPlugin_A::onGenericTestEventForComponentA);
            genericEventDispatcher.addListener(componentTypeB, TestPlugin_A::onGenericTestEventForComponentB);

            // GenericTestEvent<String> -> Static handlers (with identity param)
            genericEventDispatcher.addListener(targetTypeA, TestPlugin_A::onGenericTestEventForTargetA);
            genericEventDispatcher.addListener(targetTypeB, TestPlugin_A::onGenericTestEventForTargetB);

        }
    }

    @EventHandler
    static void onSimpleTestEventForComponentA(TestComponent_A component, SimpleTestEvent event) {
        event.setHandledBy("TestPlugin_A#onSimpleTestEventForComponentA");
    }

    @EventHandler
    static void onSimpleTestEventForComponentB(TestComponent_B component, SimpleTestEvent event) {
        event.setHandledBy("TestPlugin_A#onSimpleTestEventForComponentB");
    }

    @EventHandler
    static void onSimpleTestEventForTargetA(TestTarget_A component, SimpleTestEvent event) {
        event.setHandledBy("TestPlugin_A#onSimpleTestEventForTargetA");
    }

    @EventHandler
    static void onSimpleTestEventForTargetB(TestTarget_B component, SimpleTestEvent event) {
        event.setHandledBy("TestPlugin_A#onSimpleTestEventForTargetB");
    }

    @EventHandler
    static void onGenericTestEventForComponentA(TestComponent_A component, GenericTestEvent<String> event) {
        event.setHandledBy("TestPlugin_A#onGenericTestEventForComponentA");
    }

    @EventHandler
    static void onGenericTestEventForComponentB(TestComponent_B component, GenericTestEvent<String> event) {
        event.setHandledBy("TestPlugin_A#onGenericTestEventForComponentB");
    }

    @EventHandler
    static void onGenericTestEventForTargetA(TestTarget_A component, GenericTestEvent<String> event) {
        event.setHandledBy("TestPlugin_A#onGenericTestEventForTargetA");
    }

    @EventHandler
    static void onGenericTestEventForTargetB(TestTarget_B component, GenericTestEvent<String> event) {
        event.setHandledBy("TestPlugin_A#onGenericTestEventForTargetB");
    }

}
