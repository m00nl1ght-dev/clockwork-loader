package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.event.impl.bus.EventBusImpl;
import dev.m00nl1ght.clockwork.extension.annotations.CWLAnnotationsExtension;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.test.env.TestTarget_A;

public class AnnotatedEventHandlerTest extends AbstractEventHandlerTest {

    private EventBusImpl eventBus;

    @Override
    protected void setupComplete(ClockworkLoader loader) {
        eventBus = new EventBusImpl();
        CWLAnnotationsExtension.applyToEventBus(loader, eventBus);
        eventBus.addForwardingPolicy(targetTypeA, targetTypeC, TestTarget_A::getTestTargetC);
        super.setupComplete(loader);
    }

    @Override
    protected EventBusImpl eventBus() {
        return eventBus;
    }

}
