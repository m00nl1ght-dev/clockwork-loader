package dev.m00nl1ght.clockwork.events;

public interface GenericEvent<T> extends Event {

    Class<T> getType();

}
