package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.event.EventListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
    EventListener.Phase value() default EventListener.Phase.NORMAL;
}
