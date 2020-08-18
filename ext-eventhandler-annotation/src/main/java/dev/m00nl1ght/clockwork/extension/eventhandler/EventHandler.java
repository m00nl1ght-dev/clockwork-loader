package dev.m00nl1ght.clockwork.extension.eventhandler;

import dev.m00nl1ght.clockwork.events.EventListenerPriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
    EventListenerPriority value() default EventListenerPriority.NORMAL;
}
