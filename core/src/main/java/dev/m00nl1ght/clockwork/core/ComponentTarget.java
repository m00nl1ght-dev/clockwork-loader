package dev.m00nl1ght.clockwork.core;

public interface ComponentTarget {

    TargetType<?> getTargetType();

    Object getComponent(int internalID);

}
