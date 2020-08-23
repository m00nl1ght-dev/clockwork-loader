package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.LogUtil;
import dev.m00nl1ght.clockwork.util.Preconditions;

public class ExceptionInPlugin extends RuntimeException {

    private final LoadedPlugin plugin;

    private ExceptionInPlugin(LoadedPlugin plugin, String msg) {
        super(msg);
        this.plugin = Preconditions.notNull(plugin, "plugin");
    }

    private ExceptionInPlugin(LoadedPlugin plugin, String msg, Throwable throwable) {
        super(msg, throwable);
        this.plugin = Preconditions.notNull(plugin, "plugin");
    }

    public static ExceptionInPlugin generic(LoadedPlugin plugin, String msg, Object... objects) {
        return new ExceptionInPlugin(plugin, LogUtil.format(msg, "[]", objects));
    }

    public static ExceptionInPlugin generic(LoadedPlugin plugin, String msg, Throwable cause, Object... objects) {
        return new ExceptionInPlugin(plugin, LogUtil.format(msg, "[]", objects), cause);
    }

    public static ExceptionInPlugin inEventHandler(ComponentType component, Object event, Object target, Throwable cause) {
        final var evt = event.getClass().getSimpleName();
        return generic(component.getPlugin(), "Exception thrown while handling event [] in component []", cause, evt, component);
    }

    public static ExceptionInPlugin inComponentInit(ComponentType component, Throwable cause) {
        return generic(component.getPlugin(), "Exception thrown while initialising component []", cause, component);
    }

    public static ExceptionInPlugin inFunctionalSubtarget(ComponentType component, Class<?> type, Throwable cause) {
        return generic(component.getPlugin(), "Exception thrown while applying subtarget [] for component []", cause, type.getSimpleName(), component);
    }

}
