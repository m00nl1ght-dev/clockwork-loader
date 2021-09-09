package dev.m00nl1ght.clockwork.utils.logger.impl;

import dev.m00nl1ght.clockwork.utils.logger.FormatUtil;
import dev.m00nl1ght.clockwork.utils.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class SysOutLogging implements Logger.Factory {

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
        public void log(@NotNull Level level, @NotNull String msg, Object... objects) {
            System.out.println("[" + name + "] " + level + ": " + FormatUtil.format(msg, objects));
        }

        @Override
        public void catching(@NotNull Level level, @NotNull Throwable throwable) {
            throwable.printStackTrace();
        }

    }

}
