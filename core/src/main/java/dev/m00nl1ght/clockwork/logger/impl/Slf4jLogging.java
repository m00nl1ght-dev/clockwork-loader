package dev.m00nl1ght.clockwork.logger.impl;

import dev.m00nl1ght.clockwork.logger.Logger;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;
import java.util.function.Function;

public final class Slf4jLogging implements Function<String, Logger> {

    static {
        LoggerFactory.class.getModule();
    }

    @Override
    public Logger apply(@NotNull String loggerName) {
        final var lambda = (PrivilegedAction<org.slf4j.Logger>) () -> LoggerFactory.getLogger(loggerName);
        final var slf4j = AccessController.doPrivileged(lambda);
        return new Slf4jLogger(slf4j);
    }

    public static final class Slf4jLogger extends Logger {

        private final org.slf4j.Logger slf4j;

        private Slf4jLogger(@NotNull org.slf4j.Logger slf4j) {
            super(slf4j.getName());
            this.slf4j = Objects.requireNonNull(slf4j);
        }

        @Override
        public void debug(@NotNull String msg, Object... objects) {
            slf4j.debug(FormatUtil.format(msg, objects));
        }

        @Override
        public void info(@NotNull String msg, Object... objects) {
            slf4j.info(FormatUtil.format(msg, objects));
        }

        @Override
        public void warn(@NotNull String msg, Object... objects) {
            slf4j.warn(FormatUtil.format(msg, objects));
        }

        @Override
        public void error(@NotNull String msg, Object... objects) {
            slf4j.error(FormatUtil.format(msg, objects));
        }

        @Override
        public void throwable(@NotNull Throwable throwable) {
            slf4j.error(throwable.getClass().getSimpleName() + ": " + throwable.getMessage(), throwable);
        }

    }

}
