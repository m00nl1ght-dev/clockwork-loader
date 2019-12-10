package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.debug.DebugUtils;
import dev.m00nl1ght.clockwork.debug.profiler.DebugProfiler;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerEntry;
import dev.m00nl1ght.clockwork.debug.profiler.SimpleCyclicProfilerEntry;
import dev.m00nl1ght.clockwork.debug.profiler.generic.SimpleProfilerGroup;

import java.util.Random;

@SuppressWarnings("Convert2streamapi")
public class EventBenchmarkTest {

    private static final int dataLenght = 100;
    private static final int iterations = 1000;
    private static Runnable[] dataA;
    private static int[] dataB;

    private static int val = 0;

    public static void main(String[] args) {
        final var rand = new Random();

        final var profiler = new DebugProfiler();
        final var group = new SimpleProfilerGroup("test");
        final var entryA = new SimpleCyclicProfilerEntry("entry", 100);
        group.addEntry(entryA);
        final var npGroup = new SimpleProfilerGroup("NP");
        final var entryNP1 = new SimpleCyclicProfilerEntry("NP1", 100);
        final var entryNP2 = new SimpleCyclicProfilerEntry("NP2", 100);
        final var entryNP3 = new SimpleCyclicProfilerEntry("NP3", 100);
        npGroup.addEntries(entryNP1, entryNP2, entryNP3);
        final var epGroup = new SimpleProfilerGroup("EP");
        final var entryEP1 = new SimpleCyclicProfilerEntry("EP1", 100);
        final var entryEP2 = new SimpleCyclicProfilerEntry("EP2", 100);
        final var entryEP3 = new SimpleCyclicProfilerEntry("EP3", 100);
        epGroup.addEntries(entryEP1, entryEP2, entryEP3);
        final var cpGroup = new SimpleProfilerGroup("CP");
        final var entryCP1 = new SimpleCyclicProfilerEntry("CP1", 100);
        final var entryCP2 = new SimpleCyclicProfilerEntry("CP2", 100);
        final var entryCP3 = new SimpleCyclicProfilerEntry("CP3", 100);
        cpGroup.addEntries(entryCP1, entryCP2, entryCP3);
        profiler.addGroups(group, npGroup, epGroup, cpGroup);

        for (int k = 0; k < 100; k++) {
            System.out.println("Test " + k);
            dataA = new Runnable[dataLenght];
            for (int i = 0; i < dataA.length; i++) dataA[i] = new DoStuff(rand.nextInt(1000));
            for (int i = 0; i < dataA.length / 2; i++) dataA[rand.nextInt(dataA.length)] = new DoNothing();

            dataB = new int[dataLenght];
            for (int i = 0; i < dataB.length; i++) dataB[i] = rand.nextInt(1000);
            for (int i = 0; i < dataB.length / 2; i++) dataB[rand.nextInt(dataB.length)] = -1;

            noProfiler(entryNP1, entryNP2, entryNP3);
            withProfiler(entryA, entryCP1, entryCP2, entryCP3);
        }

        System.out.println(DebugUtils.printProfilerInfo(profiler));

    }

    private static void noProfiler(ProfilerEntry p1, ProfilerEntry p2, ProfilerEntry p3) {
        val = 0;
        var t = System.nanoTime();
        for (int r = 0; r < iterations; r++) {
            for (int i = 0; i < dataA.length; i++) {
                dataA[i].run();
            }
        }
        p1.put(System.nanoTime() - t);

        val = 0;
        t = System.nanoTime();
        for (int r = 0; r < iterations; r++) {
            for (int i = 0; i < dataB.length; i++) {
                final var d = dataB[i];
                if (d >= 0) val += d;
            }
        }
        p2.put(System.nanoTime() - t);

        val = 0;
        t = System.nanoTime();
        for (int r = 0; r < iterations; r++) {
            for (int i = 0; i < dataB.length; i++) {
                val += dataB[i];
            }
        }
        p3.put(System.nanoTime() - t);
    }

    private static void withProfiler(ProfilerEntry profiler, ProfilerEntry p1, ProfilerEntry p2, ProfilerEntry p3) {
        val = 0;
        var t = System.nanoTime();
        for (int r = 0; r < iterations; r++) {
            for (int i = 0; i < dataA.length; i++) {
                var x = System.nanoTime();
                dataA[i].run();
                profiler.put(System.nanoTime() - x);
            }
        }
        p1.put(System.nanoTime() - t);

        val = 0;
        t = System.nanoTime();
        for (int r = 0; r < iterations; r++) {
            for (int i = 0; i < dataB.length; i++) {
                var x = System.nanoTime();
                final var d = dataB[i];
                if (d >= 0) val += d;
                profiler.put(System.nanoTime() - x);
            }
        }
        p2.put(System.nanoTime() - t);

        val = 0;
        t = System.nanoTime();
        for (int r = 0; r < iterations; r++) {
            for (int i = 0; i < dataB.length; i++) {
                var x = System.nanoTime();
                val += dataB[i];
                profiler.put(System.nanoTime() - x);
            }
        }
        p3.put(System.nanoTime() - t);
    }

    static class DoStuff implements Runnable {

        final int value;

        DoStuff(int value) {this.value = value;}

        @Override
        public void run() {
            val += value;
        }

    }

    static class DoNothing implements Runnable {

        @Override
        public void run() {
            //NOOP
        }

    }

}
