package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.events.impl.EventBusImpl;
import dev.m00nl1ght.clockwork.extension.annotations.ExtEventBusImpl;

public class AnnotatedEventHandlerTest extends AbstractEventHandlerTest {

    private ExtEventBusImpl eventBus;

    @Override
    protected void setupComplete() {
        eventBus = new ExtEventBusImpl(core());
        eventBus.bind();
    }

    @Override
    protected EventBusImpl eventBus() {
        return eventBus;
    }

}
