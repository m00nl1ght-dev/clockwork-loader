package dev.m00nl1ght.clockwork.event;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
    boolean receiveCancelled = false;
}
