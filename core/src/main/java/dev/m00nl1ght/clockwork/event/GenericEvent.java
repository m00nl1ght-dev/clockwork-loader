package dev.m00nl1ght.clockwork.event;

public interface GenericEvent<T> extends Event {

    Class<T> getType();

}
