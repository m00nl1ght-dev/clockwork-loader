package dev.m00nl1ght.clockwork.utils.logger.impl;

import dev.m00nl1ght.clockwork.utils.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class SysOutLogging implements Logger.Provider {

    @Override
    public Logger getLogger(String loggerName) {
        return new SysOutLogger(loggerName);
    }

    public static final class SysOutLogger extends Logger {

        private final String name;

        private SysOutLogger(@NotNull String name) {
            this.name = Objects.requireNonNull(name);
        }

        @Override
        public @NotNull String getName() {
            return name;
        }

        @Override
        public void log(@NotNull Level level, @NotNull String message, Object... objects) {
            System.out.println("[" + name + "] " + level + ": " + formatMessage(message, objects));
        }

        @Override
        public void catching(@NotNull Level level, @NotNull Throwable throwable) {
            throwable.printStackTrace();
        }

    }

}
