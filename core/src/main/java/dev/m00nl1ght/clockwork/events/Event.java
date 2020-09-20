package dev.m00nl1ght.clockwork.events;

public abstract class Event {

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
