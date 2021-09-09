package dev.m00nl1ght.clockwork.utils.logger.impl;

import dev.m00nl1ght.clockwork.utils.logger.FormatUtil;
import dev.m00nl1ght.clockwork.utils.logger.Logger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;

public final class Slf4jLogging implements Logger.Factory {

    static {
        LoggerFactory.class.getModule();
    }

    @Override
    public Logger getLogger(@NotNull String loggerName) {
        final var lambda = (PrivilegedAction<org.slf4j.Logger>) () -> LoggerFactory.getLogger(loggerName);
        final var slf4j = AccessController.doPrivileged(lambda);
        return new Slf4jLogger(slf4j);
    }

    public static final class Slf4jLogger extends Logger {

        private final org.slf4j.Logger slf4j;

        private Slf4jLogger(@NotNull org.slf4j.Logger slf4j) {
            this.slf4j = Objects.requireNonNull(slf4j);
        }

        @Override
        public @NotNull String getName() {
            return slf4j.getName();
        }

        @Override
        public void log(@NotNull Level level, @NotNull String msg, Object... objects) {
            switch (level) {
                case DEBUG: slf4j.debug(FormatUtil.format(msg, objects)); break;
                case INFO: slf4j.info(FormatUtil.format(msg, objects)); break;
                case WARN: slf4j.warn(FormatUtil.format(msg, objects)); break;
                case ERROR: slf4j.error(FormatUtil.format(msg, objects)); break;
            }
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
        public void catching(@NotNull Level level, @NotNull Throwable throwable) {
            switch (level) {
                case DEBUG: slf4j.debug("", throwable); break;
                case INFO: slf4j.info("", throwable); break;
                case WARN: slf4j.warn("", throwable); break;
                case ERROR: slf4j.error("", throwable); break;
            }
        }

        @Override
        public void catching(@NotNull Throwable throwable) {
            slf4j.error("", throwable);
        }

    }

}
