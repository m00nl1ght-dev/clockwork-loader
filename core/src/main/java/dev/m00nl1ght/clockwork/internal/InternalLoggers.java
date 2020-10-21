package dev.m00nl1ght.clockwork.internal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InternalLoggers {

    private InternalLoggers() {}

    public static final Logger LOADER = LogManager.getLogger("Clockwork-Loader");
    public static final Logger SECURITY = LogManager.getLogger("Clockwork-Security");

}
