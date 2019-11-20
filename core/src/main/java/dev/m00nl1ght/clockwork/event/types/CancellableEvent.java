package dev.m00nl1ght.clockwork.event.types;

public interface CancellableEvent {

    boolean isCancelled();

    void setCancelled();

}
