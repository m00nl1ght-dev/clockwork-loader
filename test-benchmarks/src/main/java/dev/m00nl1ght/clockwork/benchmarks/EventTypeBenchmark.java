package dev.m00nl1ght.clockwork.benchmarks;

import dev.m00nl1ght.clockwork.benchmarks.event.*;
import dev.m00nl1ght.clockwork.debug.DebugUtils;
import dev.m00nl1ght.clockwork.debug.profiler.DebugProfiler;
import dev.m00nl1ght.clockwork.debug.profiler.SimpleProfilerEntry;
import dev.m00nl1ght.clockwork.debug.profiler.generic.SimpleProfilerGroup;
import dev.m00nl1ght.clockwork.extension.annotations.CWLAnnotationsExtension;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EventTypeBenchmark {

    private static final int ITERATIONS = 10000;

    private static final Logger LOGGER = LogManager.getLogger();

    public static final TestTarget testTarget = new TestTarget();

    public static final List<TestEventType<TestEvent, TestTarget>> eventTypesAnn = new ArrayList<>();
    public static final List<TestEventType<TestEvent, TestTarget>> eventTypesLam = new ArrayList<>();

    public static void main(String[] args) {

        eventTypesAnn.add(new EventTypeImpl0<>(TestEvent.class, TestTarget.TARGET_TYPE));
        eventTypesAnn.add(new EventTypeImpl1<>(TestEvent.class, TestTarget.TARGET_TYPE));
        eventTypesAnn.add(new EventTypeImpl2<>(TestEvent.class, TestTarget.TARGET_TYPE));
        eventTypesAnn.add(new EventTypeImpl3<>(TestEvent.class, TestTarget.TARGET_TYPE));
        eventTypesAnn.add(new EventTypeImpl4<>(TestEvent.class, testTarget));

        eventTypesLam.add(new EventTypeImpl0<>(TestEvent.class, TestTarget.TARGET_TYPE));
        eventTypesLam.add(new EventTypeImpl1<>(TestEvent.class, TestTarget.TARGET_TYPE));
        eventTypesLam.add(new EventTypeImpl2<>(TestEvent.class, TestTarget.TARGET_TYPE));
        eventTypesLam.add(new EventTypeImpl3<>(TestEvent.class, TestTarget.TARGET_TYPE));
        eventTypesLam.add(new EventTypeImpl4<>(TestEvent.class, testTarget));

        final var profiler = new DebugProfiler();

        final var profilerGroup0 = new SimpleProfilerGroup("Empty");
        profiler.addGroup(profilerGroup0);

        final var profilerGroup5 = new SimpleProfilerGroup("5 Listeners");
        profiler.addGroup(profilerGroup5);

        final var profilerGroup15 = new SimpleProfilerGroup("15 Listeners");
        profiler.addGroup(profilerGroup15);

        final var profilerGroup50 = new SimpleProfilerGroup("50 Listeners");
        profiler.addGroup(profilerGroup50);

        final var profilerGroup250 = new SimpleProfilerGroup("250 Listeners");
        profiler.addGroup(profilerGroup250);

        run(profilerGroup0);

        add(5);
        run(profilerGroup5);

        add(10);
        run(profilerGroup15);

        add(35);
        run(profilerGroup50);

        add(200);
        run(profilerGroup250);

        System.out.println(DebugUtils.printProfilerInfo(profiler));
        for (final var group : profiler.getGroups()) {
            DebugUtils.writeProfilerInfoToCSV(group, new File("test-benchmarks/results/", group.getName() + ".csv"));
        }

    }

    private static void run(SimpleProfilerGroup profilerGroup) {

        for (final var eventType : eventTypesAnn) {
            final var profilerEntry = new SimpleProfilerEntry(eventType.getClass().getSimpleName() + "-ANNC");
            profilerGroup.addEntry(profilerEntry);
            for (int i = 0; i < ITERATIONS; i++) {
                final var t = System.nanoTime();
                eventType.post(testTarget, new TestEvent());
                profilerEntry.put(System.nanoTime() - t);
            }
        }

        for (final var eventType : eventTypesLam) {
            final var profilerEntry = new SimpleProfilerEntry(eventType.getClass().getSimpleName() + "-LAMC");
            profilerGroup.addEntry(profilerEntry);
            for (int i = 0; i < ITERATIONS; i++) {
                final var t = System.nanoTime();
                eventType.post(testTarget, new TestEvent());
                profilerEntry.put(System.nanoTime() - t);
            }
        }

        for (final var eventType : eventTypesAnn) {
            final var profilerEntry = new SimpleProfilerEntry(eventType.getClass().getSimpleName() + "-ANNN");
            profilerGroup.addEntry(profilerEntry);
            for (int i = 0; i < ITERATIONS; i++) {
                final var t = System.nanoTime();
                eventType.postContextless(testTarget, new TestEvent());
                profilerEntry.put(System.nanoTime() - t);
            }
        }

        for (final var eventType : eventTypesLam) {
            final var profilerEntry = new SimpleProfilerEntry(eventType.getClass().getSimpleName() + "-LAMN");
            profilerGroup.addEntry(profilerEntry);
            for (int i = 0; i < ITERATIONS; i++) {
                final var t = System.nanoTime();
                eventType.postContextless(testTarget, new TestEvent());
                profilerEntry.put(System.nanoTime() - t);
            }
        }

    }

    private static void add(int its) {
        for (int i = 0; i < its; i++) {
            CWLAnnotationsExtension.buildListeners(ClockworkBenchmarks.clockworkCore, eventTypesAnn);
            eventTypesLam.forEach(TestComponent::registerLambda);
        }
    }

}
