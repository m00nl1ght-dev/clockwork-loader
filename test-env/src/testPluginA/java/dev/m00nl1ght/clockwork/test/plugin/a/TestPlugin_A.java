package dev.m00nl1ght.clockwork.test.plugin.a;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.MainComponent;
import dev.m00nl1ght.clockwork.extension.annotations.EventHandler;
import dev.m00nl1ght.clockwork.test.env.*;
import dev.m00nl1ght.clockwork.test.env.events.GenericTestEvent;
import dev.m00nl1ght.clockwork.test.env.events.SimpleTestEvent;
import dev.m00nl1ght.clockwork.test.env.security.PermissionTestEvent;

import java.io.FileWriter;
import java.io.IOException;

public class TestPlugin_A extends MainComponent {

    public TestPlugin_A(ClockworkCore core) {
        super(core);
        final var eventBus = TestEnvironment.get(core).getTestEventBus();
        if (eventBus != null) {

            final var targetTypeA = core.getTargetTypeOrThrow(TestTarget_A.class);
            final var targetTypeB = core.getTargetTypeOrThrow(TestTarget_B.class);
            final var targetTypeC = core.getTargetTypeOrThrow(TestTarget_C.class);
            final var targetTypeD = core.getTargetTypeOrThrow(TestTarget_D.class);

            // SimpleTestEvent -> Component instance handlers
            eventBus.addListener(SimpleTestEvent.class, targetTypeA, TestComponent_A.class, TestComponent_A::onSimpleTestEvent);
            eventBus.addListener(SimpleTestEvent.class, targetTypeB, TestComponent_B.class, TestComponent_B::onSimpleTestEvent);

            // SimpleTestEvent -> Static handlers (with component param)
            eventBus.addListener(SimpleTestEvent.class, targetTypeA, TestComponent_A.class, TestPlugin_A::onSimpleTestEventForComponentA);
            eventBus.addListener(SimpleTestEvent.class, targetTypeB, TestComponent_B.class, TestPlugin_A::onSimpleTestEventForComponentB);

            // SimpleTestEvent -> Static handlers (with identity param)
            eventBus.addListener(SimpleTestEvent.class, targetTypeA, TestTarget_A.class, TestPlugin_A::onSimpleTestEventForTargetA);
            eventBus.addListener(SimpleTestEvent.class, targetTypeB, TestTarget_B.class, TestPlugin_A::onSimpleTestEventForTargetB);

            // SimpleTestEvent -> Nested handler in TestTarget_C
            eventBus.addListener(SimpleTestEvent.class, targetTypeC, TestComponent_C.class, TestComponent_C::onSimpleTestEvent);
            eventBus.addListener(SimpleTestEvent.class, targetTypeD, TestComponent_D.class, TestComponent_D::onSimpleTestEvent);

            // GenericTestEvent<String> -> Component instance handlers
            eventBus.addListener(GenericTestEvent.STRING_TYPE, targetTypeA, TestComponent_A.class, TestComponent_A::onGenericTestEvent);
            eventBus.addListener(GenericTestEvent.STRING_TYPE, targetTypeB, TestComponent_B.class, TestComponent_B::onGenericTestEvent);

            // GenericTestEvent<String> -> Static handlers (with component param)
            eventBus.addListener(GenericTestEvent.STRING_TYPE, targetTypeA, TestComponent_A.class, TestPlugin_A::onGenericTestEventForComponentA);
            eventBus.addListener(GenericTestEvent.STRING_TYPE, targetTypeB, TestComponent_B.class, TestPlugin_A::onGenericTestEventForComponentB);

            // GenericTestEvent<String> -> Static handlers (with identity param)
            eventBus.addListener(GenericTestEvent.STRING_TYPE, targetTypeA, TestTarget_A.class, TestPlugin_A::onGenericTestEventForTargetA);
            eventBus.addListener(GenericTestEvent.STRING_TYPE, targetTypeB, TestTarget_B.class, TestPlugin_A::onGenericTestEventForTargetB);

            // GenericTestEvent<String> -> Nested handler in TestTarget_C
            eventBus.addListener(GenericTestEvent.STRING_TYPE, targetTypeC, TestComponent_C.class, TestComponent_C::onGenericTestEvent);
            eventBus.addListener(GenericTestEvent.STRING_TYPE, targetTypeD, TestComponent_D.class, TestComponent_D::onGenericTestEvent);

        }
    }

    @EventHandler
    static void onSimpleTestEventForComponentA(TestComponent_A component, SimpleTestEvent event) {
        event.getTestContext().addMarker("TestPlugin_A#onSimpleTestEventForComponentA");
    }

    @EventHandler
    static void onSimpleTestEventForComponentB(TestComponent_B component, SimpleTestEvent event) {
        event.getTestContext().addMarker("TestPlugin_A#onSimpleTestEventForComponentB");
    }

    @EventHandler
    static void onSimpleTestEventForTargetA(TestTarget_A component, SimpleTestEvent event) {
        event.getTestContext().addMarker("TestPlugin_A#onSimpleTestEventForTargetA");
    }

    @EventHandler
    static void onSimpleTestEventForTargetB(TestTarget_B component, SimpleTestEvent event) {
        event.getTestContext().addMarker("TestPlugin_A#onSimpleTestEventForTargetB");
    }

    @EventHandler
    static void onGenericTestEventForComponentA(TestComponent_A component, GenericTestEvent<String> event) {
        event.getTestContext().addMarker("TestPlugin_A#onGenericTestEventForComponentA");
    }

    @EventHandler
    static void onGenericTestEventForComponentB(TestComponent_B component, GenericTestEvent<String> event) {
        event.getTestContext().addMarker("TestPlugin_A#onGenericTestEventForComponentB");
    }

    @EventHandler
    static void onGenericTestEventForTargetA(TestTarget_A component, GenericTestEvent<String> event) {
        event.getTestContext().addMarker("TestPlugin_A#onGenericTestEventForTargetA");
    }

    @EventHandler
    static void onGenericTestEventForTargetB(TestTarget_B component, GenericTestEvent<String> event) {
        event.getTestContext().addMarker("TestPlugin_A#onGenericTestEventForTargetB");
    }

    @EventHandler
    void onPermissionTest(PermissionTestEvent event) {
        final var file = event.getTestFile();
        file.getParentFile().mkdirs();

        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test file", e);
        }

        try (var fileWriter = new FileWriter(file)) {
            fileWriter.write("Hello from TestPlugin_A!");
        } catch (IOException e) {
            throw new RuntimeException("Failed to write test file", e);
        }
    }

}
