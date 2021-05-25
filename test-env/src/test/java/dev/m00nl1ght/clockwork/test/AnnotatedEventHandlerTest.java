package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.event.impl.bus.EventBusImpl;
import dev.m00nl1ght.clockwork.extension.annotations.CWLAnnotationsExtension;

public class AnnotatedEventHandlerTest extends AbstractEventHandlerTest {

    private EventBusImpl eventBus;

    @Override
    protected void setupComplete() {
        eventBus = new EventBusImpl();
        CWLAnnotationsExtension.applyToEventBus(core(), eventBus);
        super.setupComplete();
    }

    @Override
    protected EventBusImpl eventBus() {
        return eventBus;
    }

}
