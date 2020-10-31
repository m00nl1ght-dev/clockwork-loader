package dev.m00nl1ght.clockwork.test.env.events;

import dev.m00nl1ght.clockwork.events.impl.ContextAwareEvent;

import java.util.HashSet;
import java.util.Set;

public abstract class TestEvent extends ContextAwareEvent {

    private final Set<String> handledBy = new HashSet<>();

    public void setHandledBy(String handler) {
        if (!handledBy.add(handler)) {
            throw new IllegalStateException("Event was already handled by: " + handler);
        }
    }

    public boolean wasHandledBy(String handler) {
        return handledBy.contains(handler);
    }

}
