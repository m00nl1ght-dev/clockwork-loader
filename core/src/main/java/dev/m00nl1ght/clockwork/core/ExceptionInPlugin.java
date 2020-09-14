package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.events.EventListener;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.Arguments;

public class ExceptionInPlugin extends RuntimeException {

    // TODO add trace list for other plugins that were in the stack

    private final LoadedPlugin plugin;

    private ExceptionInPlugin(LoadedPlugin plugin, String msg) {
        super(msg);
        this.plugin = Arguments.notNull(plugin, "plugin");
    }

    private ExceptionInPlugin(LoadedPlugin plugin, String msg, Throwable throwable) {
        super(msg, throwable);
        this.plugin = Arguments.notNull(plugin, "plugin");
    }

    public static ExceptionInPlugin generic(LoadedPlugin plugin, String msg, Object... objects) {
        return new ExceptionInPlugin(plugin, FormatUtil.format(msg, objects));
    }

    public static ExceptionInPlugin generic(LoadedPlugin plugin, String msg, Throwable cause, Object... objects) {
        return new ExceptionInPlugin(plugin, FormatUtil.format(msg, objects), cause);
    }

    public static ExceptionInPlugin inEventListener(EventListener<?, ?, ?> listener, Object event, Object target, Throwable cause) {
        return generic(listener.getComponentType().getPlugin(), "Exception thrown in event listener [] while handling event []", cause, listener, event);
    }

    public static ExceptionInPlugin inComponentInit(ComponentType component, Throwable cause) {
        return generic(component.getPlugin(), "Exception thrown while initialising component []", cause, component);
    }

    public static ExceptionInPlugin inComponentInterface(ComponentType component, Class<?> interfaceType, Throwable cause) {
        return generic(component.getPlugin(), "Exception thrown while applying interface [] for component []", cause, interfaceType.getSimpleName(), component);
    }

    public LoadedPlugin getPlugin() {
        return plugin;
    }

}
