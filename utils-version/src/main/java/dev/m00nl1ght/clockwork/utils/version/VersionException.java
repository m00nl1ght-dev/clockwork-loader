package dev.m00nl1ght.clockwork.utils.version;

public class VersionException extends RuntimeException {

    VersionException(String msg) {
        super(msg);
    }

    VersionException(String msg, Throwable t) {
        super(msg, t);
    }

}
