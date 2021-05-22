package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.events.impl.AdvancedEventBusImpl;
import dev.m00nl1ght.clockwork.events.impl.EventBusImpl;
import dev.m00nl1ght.clockwork.extension.annotations.CWLAnnotationsExtension;

public class AnnotatedEventHandlerTest extends AbstractEventHandlerTest {

    private EventBusImpl eventBus;

    @Override
    protected void setupComplete() {
        eventBus = new AdvancedEventBusImpl();
        CWLAnnotationsExtension.applyToEventBus(core(), eventBus);
        super.setupComplete();
    }

    @Override
    protected EventBusImpl eventBus() {
        return eventBus;
    }

}
