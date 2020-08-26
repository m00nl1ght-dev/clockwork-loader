package dev.m00nl1ght.clockwork.benchmarks;

import dev.m00nl1ght.clockwork.benchmarks.event.EventTypeImpl0;
import dev.m00nl1ght.clockwork.debug.DebugUtils;
import dev.m00nl1ght.clockwork.debug.profiler.DebugProfiler;
import dev.m00nl1ght.clockwork.debug.profiler.SimpleCyclicProfilerEntry;
import dev.m00nl1ght.clockwork.debug.profiler.generic.SimpleProfilerGroup;
import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.extension.annotations.CWLAnnotationsExtension;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class EventTypeBenchmark {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final TestTarget testTarget = new TestTarget();

    public static final List<EventType<TestEvent, TestTarget>> eventTypes = new ArrayList<>();

    public static void main(String[] args) {

        eventTypes.add(new EventTypeImpl0<>(TestEvent.class, TestTarget.TARGET_TYPE));

        final var profiler = new DebugProfiler();

        final var profilerGroupE = new SimpleProfilerGroup("Empty");
        profiler.addGroup(profilerGroupE);

        final var profilerGroupA = new SimpleProfilerGroup("All");
        profiler.addGroup(profilerGroupA);

        for (final var eventType : eventTypes) {
            final var profilerEntry = new SimpleCyclicProfilerEntry(eventType.getClass().getSimpleName(), 100);
            profilerGroupE.addEntry(profilerEntry);
            for (int i = 0; i < 100; i++) {
                final var t = System.nanoTime();
                eventType.post(testTarget, new TestEvent());
                profilerEntry.put(System.nanoTime() - t);
            }
        }

        for (final var eventType : eventTypes) {
            CWLAnnotationsExtension.fetchListeners(ClockworkBenchmarks.annotationProcessor, eventType);
        }

        for (final var eventType : eventTypes) {
            final var profilerEntry = new SimpleCyclicProfilerEntry(eventType.getClass().getSimpleName(), 100);
            profilerGroupA.addEntry(profilerEntry);
            for (int i = 0; i < 100; i++) {
                final var t = System.nanoTime();
                eventType.post(testTarget, new TestEvent());
                profilerEntry.put(System.nanoTime() - t);
            }
        }

        System.out.println(DebugUtils.printProfilerInfo(profiler));

    }

}
