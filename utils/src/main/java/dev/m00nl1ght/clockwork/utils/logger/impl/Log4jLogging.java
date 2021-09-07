package dev.m00nl1ght.clockwork.utils.logger.impl;

import dev.m00nl1ght.clockwork.utils.logger.Logger;
import dev.m00nl1ght.clockwork.utils.logger.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;
import java.util.function.Function;

public final class Log4jLogging implements Function<String, Logger> {

    static {
        LogManager.class.getModule();
    }

    @Override
    public Logger apply(@NotNull String loggerName) {
        final var lambda = (PrivilegedAction<org.apache.logging.log4j.Logger>) () -> LogManager.getLogger(loggerName);
        final var log4j = AccessController.doPrivileged(lambda);
        return new Log4jLogger(log4j);
    }

    public static final class Log4jLogger extends Logger {

        private final org.apache.logging.log4j.Logger log4j;

        private Log4jLogger(@NotNull org.apache.logging.log4j.Logger log4j) {
            super(log4j.getName());
            this.log4j = Objects.requireNonNull(log4j);
        }

        @Override
        public void debug(@NotNull String msg, Object... objects) {
            log4j.debug(FormatUtil.format(msg, objects));
        }

        @Override
        public void info(@NotNull String msg, Object... objects) {
            log4j.info(FormatUtil.format(msg, objects));
        }

        @Override
        public void warn(@NotNull String msg, Object... objects) {
            log4j.warn(FormatUtil.format(msg, objects));
        }

        @Override
        public void error(@NotNull String msg, Object... objects) {
            log4j.error(FormatUtil.format(msg, objects));
        }

        @Override
        public void throwable(@NotNull Throwable throwable) {
            log4j.catching(throwable);
        }

    }

}
