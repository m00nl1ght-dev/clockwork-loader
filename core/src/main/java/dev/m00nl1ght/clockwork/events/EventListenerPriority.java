package dev.m00nl1ght.clockwork.events;

public enum EventListenerPriority {

    PRE(false),
    EARLY(true),
    NORMAL(true),
    LATE(true),
    POST(false);

    private final boolean modificationAllowed;

    EventListenerPriority(boolean modificationAllowed) {
        this.modificationAllowed = modificationAllowed;
    }

    public boolean isModificationAllowed() {
        return modificationAllowed;
    }

}
