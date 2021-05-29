package dev.m00nl1ght.clockwork.logger.impl;

import dev.m00nl1ght.clockwork.logger.Logger;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class SysOutLogging implements Function<String, Logger> {

    @Override
    public Logger apply(String loggerName) {
        return new SysOutLogger(loggerName);
    }

    public static final class SysOutLogger extends Logger {

        private SysOutLogger(@NotNull String name) {
            super(name);
        }

        @Override
        public void debug(@NotNull String msg, Object... objects) {
            System.out.println("DEBUG [" + name + "] " + FormatUtil.format(msg, objects));
        }

        @Override
        public void info(@NotNull String msg, Object... objects) {
            System.out.println("INFO  [" + name + "] " + FormatUtil.format(msg, objects));
        }

        @Override
        public void warn(@NotNull String msg, Object... objects) {
            System.out.println("WARN  [" + name + "] " + FormatUtil.format(msg, objects));
        }

        @Override
        public void error(@NotNull String msg, Object... objects) {
            System.out.println("ERROR [" + name + "] " + FormatUtil.format(msg, objects));
        }

        @Override
        public void throwable(@NotNull Throwable throwable) {
            throwable.printStackTrace();
        }

    }

}
