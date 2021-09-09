package dev.m00nl1ght.clockwork.utils.logger;

import dev.m00nl1ght.clockwork.utils.logger.impl.SysOutLogging;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Logger {

    private static final String LOG4J_SUPPORT = "dev.m00nl1ght.clockwork.utils.logger.impl.Log4jLogging";
    private static final String SLF4J_SUPPORT = "dev.m00nl1ght.clockwork.utils.logger.impl.Slf4jLogging";

    private static Factory loggerFactory;

    static { init(); }
    private static void init() {
        if (tryLoadFactory(LOG4J_SUPPORT)) return;
        if (tryLoadFactory(SLF4J_SUPPORT)) return;
        loggerFactory = new SysOutLogging();
    }

    public static void setFactory(@NotNull Factory factory) {
        loggerFactory = Objects.requireNonNull(factory);
    }

    public static boolean tryLoadFactory(@NotNull String className) {
        return tryLoadFactory(Logger.class.getClassLoader(), className);
    }

    public static boolean tryLoadFactory(@NotNull ClassLoader classLoader, @NotNull String className) {
        try {
            final var factoryClass = classLoader.loadClass(Objects.requireNonNull(className));
            final var constr = factoryClass.getConstructor();
            final var inst = (Factory) constr.newInstance();
            loggerFactory = inst;
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isFallbackMode() {
        return loggerFactory instanceof SysOutLogging;
    }

    public static Logger getLogger(@NotNull String name) {
        return loggerFactory.getLogger(Objects.requireNonNull(name));
    }

    public abstract @NotNull String getName();

    public abstract void log(@NotNull Level level, @NotNull String msg, Object... objects);

    public void debug(@NotNull String msg, Object... objects) {
        log(Level.DEBUG, msg, objects);
    }

    public void info(@NotNull String msg, Object... objects) {
        log(Level.INFO, msg, objects);
    }

    public void warn(@NotNull String msg, Object... objects) {
        log(Level.WARN, msg, objects);
    }

    public void error(@NotNull String msg, Object... objects) {
        log(Level.ERROR, msg, objects);
    }

    public abstract void catching(@NotNull Level level, @NotNull Throwable throwable);

    public void catching(@NotNull Throwable throwable) {
        catching(Level.ERROR, throwable);
    }

    public interface Factory {
        @NotNull Logger getLogger(@NotNull String name);
    }

    public enum Level {
        ERROR, WARN, INFO, DEBUG
    }

}
