package dev.m00nl1ght.clockwork.logger;

import dev.m00nl1ght.clockwork.logger.impl.SysOutLogging;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public abstract class Logger {

    private static final String LOG4J_SUPPORT = "dev.m00nl1ght.clockwork.logger.impl.Log4jLogging";

    private static Function<String, Logger> loggerFactory;

    static { init(); }
    private static void init() {
        if (tryLoadFactory(LOG4J_SUPPORT)) return;
        loggerFactory = new SysOutLogging();
    }

    private static boolean tryLoadFactory(String className) {
        try {
            final var factoryClass = Logger.class.getClassLoader().loadClass(className);
            final var constr = factoryClass.getConstructor();
            @SuppressWarnings("unchecked")
            final var inst = (Function<String, Logger>) constr.newInstance();
            loggerFactory = inst;
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static Logger create(String name) {
        return loggerFactory.apply(name);
    }

    protected final String name;

    protected Logger(@NotNull String name) {
        this.name = Objects.requireNonNull(name);
    }

    public abstract void debug(@NotNull String msg, Object... objects);

    public abstract void info(@NotNull String msg, Object... objects);

    public abstract void warn(@NotNull String msg, Object... objects);

    public abstract void error(@NotNull String msg, Object... objects);

    public abstract void throwable(@NotNull Throwable throwable);

    public @NotNull String getName() {
        return name;
    }

}
