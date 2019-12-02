package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.debug.*;

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
        final var group = new ProfilerGroup(profiler, "test");
        final var entryA = new SimpleCyclicProfilerEntry(group, "entry", 100);
        final var entryB = NoOpProfilerEntry.INSTANCE;

        final var npGroup = new ProfilerGroup(profiler, "NP");
        final var entryNP1 = new SimpleCyclicProfilerEntry(npGroup, "NP1", 100);
        final var entryNP2 = new SimpleCyclicProfilerEntry(npGroup, "NP2", 100);
        final var entryNP3 = new SimpleCyclicProfilerEntry(npGroup, "NP3", 100);
        final var epGroup = new ProfilerGroup(profiler, "EP");
        final var entryEP1 = new SimpleCyclicProfilerEntry(epGroup, "EP1", 100);
        final var entryEP2 = new SimpleCyclicProfilerEntry(epGroup, "EP2", 100);
        final var entryEP3 = new SimpleCyclicProfilerEntry(epGroup, "EP3", 100);
        final var cpGroup = new ProfilerGroup(profiler, "CP");
        final var entryCP1 = new SimpleCyclicProfilerEntry(cpGroup, "CP1", 100);
        final var entryCP2 = new SimpleCyclicProfilerEntry(cpGroup, "CP2", 100);
        final var entryCP3 = new SimpleCyclicProfilerEntry(cpGroup, "CP3", 100);

        for (int k = 0; k < 100; k++) {
            System.out.println("Test " + k);
            dataA = new Runnable[dataLenght];
            for (int i = 0; i < dataA.length; i++) dataA[i] = new DoStuff(rand.nextInt(1000));
            for (int i = 0; i < dataA.length / 2; i++) dataA[rand.nextInt(dataA.length)] = new DoNothing();

            dataB = new int[dataLenght];
            for (int i = 0; i < dataB.length; i++) dataB[i] = rand.nextInt(1000);
            for (int i = 0; i < dataB.length / 2; i++) dataB[rand.nextInt(dataB.length)] = -1;

            noProfiler(entryNP1, entryNP2, entryNP3);
            withProfiler(entryB, entryEP1, entryEP2, entryEP3);
            withProfiler(entryA, entryCP1, entryCP2, entryCP3);
        }

        System.out.println(profiler.print());

    }

    private static void noProfiler(ProfilerEntry p1, ProfilerEntry p2, ProfilerEntry p3) {
        val = 0;
        p1.start();
        for (int r = 0; r < iterations; r++) {
            for (int i = 0; i < dataA.length; i++) {
                dataA[i].run();
            }
        }
        p1.end();

        val = 0;
        p2.start();
        for (int r = 0; r < iterations; r++) {
            for (int i = 0; i < dataB.length; i++) {
                final var d = dataB[i];
                if (d >= 0) val += d;
            }
        }
        p2.end();

        val = 0;
        p3.start();
        for (int r = 0; r < iterations; r++) {
            for (int i = 0; i < dataB.length; i++) {
                val += dataB[i];
            }
        }
        p3.end();
    }

    private static void withProfiler(ProfilerEntry profiler, ProfilerEntry p1, ProfilerEntry p2, ProfilerEntry p3) {
        val = 0;
        p1.start();
        for (int r = 0; r < iterations; r++) {
            for (int i = 0; i < dataA.length; i++) {
                profiler.start();
                dataA[i].run();
                profiler.end();
            }
        }
        p1.end();

        val = 0;
        p2.start();
        for (int r = 0; r < iterations; r++) {
            for (int i = 0; i < dataB.length; i++) {
                profiler.start();
                final var d = dataB[i];
                if (d >= 0) val += d;
                profiler.end();
            }
        }
        p2.end();

        val = 0;
        p3.start();
        for (int r = 0; r < iterations; r++) {
            for (int i = 0; i < dataB.length; i++) {
                profiler.start();
                val += dataB[i];
                profiler.end();
            }
        }
        p3.end();
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
