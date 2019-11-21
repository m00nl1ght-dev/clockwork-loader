package dev.m00nl1ght.clockwork.event;

public interface CancellableEvent extends Event {

    boolean isCancelled();

    void setCancelled();

}
