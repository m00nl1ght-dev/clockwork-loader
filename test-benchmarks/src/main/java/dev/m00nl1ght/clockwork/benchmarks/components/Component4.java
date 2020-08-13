package dev.m00nl1ght.clockwork.benchmarks.components;

import dev.m00nl1ght.clockwork.benchmarks.TestEvent;
import dev.m00nl1ght.clockwork.events.annotation.EventHandler;
import dev.m00nl1ght.clockwork.events.EventListenerPriority;

public class Component4 {

    private int received = 0;

    @EventHandler(EventListenerPriority.PRE)
    protected void onTestEvent0(TestEvent event) {
        received++;
    }

    @EventHandler(EventListenerPriority.EARLY)
    protected void onTestEvent1(TestEvent event) {
        received++;
    }

    @EventHandler(EventListenerPriority.NORMAL)
    protected void onTestEvent2(TestEvent event) {
        received++;
    }

    @EventHandler(EventListenerPriority.LATE)
    protected void onTestEvent3(TestEvent event) {
        received++;
    }

    @EventHandler(EventListenerPriority.POST)
    protected void onTestEvent4(TestEvent event) {
        received++;
    }

}
