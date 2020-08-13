package dev.m00nl1ght.clockwork.benchmarks;

import dev.m00nl1ght.clockwork.benchmarks.event.SimpleEventType;
import dev.m00nl1ght.clockwork.events.EventType;

import java.util.ArrayList;
import java.util.List;

public class EventTypeBenchmark {

    public static final TestTarget testTarget = new TestTarget();

    public static final List<EventType<TestEvent, TestTarget>> eventTypes = new ArrayList<>();

    public static void main(String[] args) {
        eventTypes.add(new SimpleEventType<>(TestEvent.class, TestTarget.class));

        for (final var eventType : eventTypes) {

        }
    }

}
