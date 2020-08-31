package dev.m00nl1ght.clockwork.benchmarks;

import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.extension.annotations.EventHandler;

public class TestComponent {

    private int receivedAnn = 0, receivedLam = 0;

    @EventHandler
    private void onTestEventAnnotated(TestEvent event) {
        receivedAnn++;
    }

    private void onTestEventLambda(TestEvent event) {
        receivedLam++;
    }

    public static void registerLambda(EventType<TestEvent, TestTarget> eventType) {
        eventType.addListener(TestComponent.class, TestComponent::onTestEventLambda);
    }

}
