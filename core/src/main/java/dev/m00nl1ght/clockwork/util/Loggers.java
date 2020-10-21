package dev.m00nl1ght.clockwork.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class Loggers {

    public static Logger getLogger(String name) {
        return AccessController.doPrivileged((PrivilegedAction<Logger>) () -> LogManager.getLogger(name));
    }

    public static Logger getLogger(Class<?> clazz) {
        return AccessController.doPrivileged((PrivilegedAction<Logger>) () -> LogManager.getLogger(clazz));
    }

}
