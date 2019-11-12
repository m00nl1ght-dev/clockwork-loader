package dev.m00nl1ght.clockwork.event.types;

import dev.m00nl1ght.clockwork.event.Event;

public abstract class CancellableEvent extends Event {

    private boolean cancelled = false;

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled() {
        cancelled = true;
    }

}
