package dev.m00nl1ght.clockwork.utils.logger.impl;

import dev.m00nl1ght.clockwork.utils.logger.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Log4jLogging implements Logger.Provider {

    static {
        LogManager.class.getModule();
    }

    @Override
    public Logger getLogger(@NotNull String loggerName) {
        return new Log4jLogger(LogManager.getLogger(loggerName));
    }

    public static Level toLog4j(@NotNull Logger.Level level) {
        switch (level) {
            case DEBUG: return Level.DEBUG;
            case INFO: return Level.INFO;
            case WARN: return Level.WARN;
            case ERROR: return Level.ERROR;
            default: throw new IllegalStateException();
        }
    }

    public static final class Log4jLogger extends Logger {

        private final org.apache.logging.log4j.Logger log4j;

        private Log4jLogger(@NotNull org.apache.logging.log4j.Logger log4j) {
            this.log4j = Objects.requireNonNull(log4j);
        }

        @Override
        public @NotNull String getName() {
            return log4j.getName();
        }

        @Override
        public void log(@NotNull Level level, @NotNull String message, Object... objects) {
            log4j.log(toLog4j(level), formatMessage(message, objects));
        }

        @Override
        public void debug(@NotNull String message, Object... objects) {
            log4j.debug(formatMessage(message, objects));
        }

        @Override
        public void info(@NotNull String message, Object... objects) {
            log4j.info(formatMessage(message, objects));
        }

        @Override
        public void warn(@NotNull String message, Object... objects) {
            log4j.warn(formatMessage(message, objects));
        }

        @Override
        public void error(@NotNull String message, Object... objects) {
            log4j.error(formatMessage(message, objects));
        }

        @Override
        public void catching(@NotNull Level level, @NotNull Throwable throwable) {
            log4j.catching(toLog4j(level), throwable);
        }

        @Override
        public void catching(@NotNull Throwable throwable) {
            log4j.catching(throwable);
        }

    }

}
