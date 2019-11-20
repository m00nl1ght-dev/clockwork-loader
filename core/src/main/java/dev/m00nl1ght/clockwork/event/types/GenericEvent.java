package dev.m00nl1ght.clockwork.event.types;

public interface GenericEvent<T> {

    Class<T> getType();

}
