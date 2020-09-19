package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.util.FormatUtil;

public abstract class ContextAwareEvent extends Event {

    ListenerList currentContext;
    int currentListenerIdx = -1;

    protected EventListener<?, ?, ?> getCurrentListener() {
        try {
            return currentContext.listeners.get(currentListenerIdx);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            throw FormatUtil.rtExc("Event listener context is missing or currupted");
        }
    }

    protected void checkModificationAllowed() {
        if (currentContext == null) return;
        final var priority = getCurrentListener().getPriority();
        if (!priority.isModificationAllowed()) {
            throw FormatUtil.illStateExc("Event can not be modified in state []", priority);
        }
    }

}
