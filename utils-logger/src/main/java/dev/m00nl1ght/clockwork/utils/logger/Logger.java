package dev.m00nl1ght.clockwork.utils.logger;

import dev.m00nl1ght.clockwork.utils.logger.impl.SysOutLogging;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Logger {

    private static final String LOG4J_SUPPORT = "dev.m00nl1ght.clockwork.utils.logger.impl.Log4jLogging";
    private static final String SLF4J_SUPPORT = "dev.m00nl1ght.clockwork.utils.logger.impl.Slf4jLogging";

    private static Provider loggerProvider;

    static { init(); }
    private static void init() {
        if (tryLoadProvider(LOG4J_SUPPORT)) return;
        if (tryLoadProvider(SLF4J_SUPPORT)) return;
        loggerProvider = new SysOutLogging();
    }

    public static void setProvider(@NotNull Logger.Provider provider) {
        loggerProvider = Objects.requireNonNull(provider);
    }

    public static boolean tryLoadProvider(@NotNull String className) {
        return tryLoadProvider(Logger.class.getClassLoader(), className);
    }

    public static boolean tryLoadProvider(@NotNull ClassLoader classLoader, @NotNull String className) {
        try {
            final var factoryClass = classLoader.loadClass(Objects.requireNonNull(className));
            final var constr = factoryClass.getConstructor();
            final var inst = (Provider) constr.newInstance();
            setProvider(inst);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isFallbackMode() {
        return loggerProvider instanceof SysOutLogging;
    }

    public static Logger getLogger(@NotNull String name) {
        return loggerProvider.getLogger(Objects.requireNonNull(name));
    }

    public abstract @NotNull String getName();

    public abstract void log(@NotNull Level level, @NotNull String message, Object... objects);

    public void debug(@NotNull String message, Object... objects) {
        log(Level.DEBUG, message, objects);
    }

    public void info(@NotNull String message, Object... objects) {
        log(Level.INFO, message, objects);
    }

    public void warn(@NotNull String message, Object... objects) {
        log(Level.WARN, message, objects);
    }

    public void error(@NotNull String message, Object... objects) {
        log(Level.ERROR, message, objects);
    }

    public abstract void catching(@NotNull Level level, @NotNull Throwable throwable);

    public void catching(@NotNull Throwable throwable) {
        catching(Level.ERROR, throwable);
    }

    public interface Provider {
        @NotNull Logger getLogger(@NotNull String name);
    }

    public enum Level {
        ERROR, WARN, INFO, DEBUG
    }

    public static String formatMessage(String message, Object... objects) {

        final var sb = new StringBuilder(message);

        var p = 0;
        var keepBr = false;

        for (var object : objects) {

            final var max = sb.length() - 1;
            for (int i = p; i < max; i++) {
                if (sb.charAt(i) == '[' && sb.charAt(i + 1) == ']') {
                    p = i; keepBr = true; break;
                } else if (sb.charAt(i) == '{' && sb.charAt(i+ 1) == '}') {
                    p = i; keepBr = false; break;
                }
            }

            if (p >= max) break;

            final var os = object.toString();
            if (keepBr) {
                sb.insert(p + 1, os);
            } else {
                sb.replace(p, p + 2, os);
            }

            p += os.length() + (keepBr ? 2 : 0);
        }

        return sb.toString();
    }

}
