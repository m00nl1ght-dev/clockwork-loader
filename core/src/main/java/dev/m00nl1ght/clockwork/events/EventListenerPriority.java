package dev.m00nl1ght.clockwork.events;

public enum EventListenerPriority {

    PRE(false),
    EARLY(true),
    NORMAL(true),
    LATE(true),
    POST(false);

    private final boolean changeAllowed;

    EventListenerPriority(boolean changeAllowed) {
        this.changeAllowed = changeAllowed;
    }

    public boolean isChangeAllowed() {
        return changeAllowed;
    }

}
