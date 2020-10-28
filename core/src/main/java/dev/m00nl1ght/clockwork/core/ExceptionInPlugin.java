package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ExceptionInPlugin extends RuntimeException {

    private final List<LoadedPlugin> stack = new LinkedList<>();

    private ExceptionInPlugin(LoadedPlugin plugin, String msg) {
        super(msg);
        addPluginToStack(plugin);
    }

    private ExceptionInPlugin(LoadedPlugin plugin, String msg, Throwable throwable) {
        super(msg, throwable);
        addPluginToStack(plugin);
    }

    public static ExceptionInPlugin generic(LoadedPlugin plugin, String msg, Object... objects) {
        return new ExceptionInPlugin(plugin, FormatUtil.format(msg, objects));
    }

    public static ExceptionInPlugin generic(LoadedPlugin plugin, String msg, Throwable cause, Object... objects) {
        return new ExceptionInPlugin(plugin, FormatUtil.format(msg, objects), cause);
    }

    public static RuntimeException genericOrRt(ComponentType componentType, String msg, Throwable cause, Object... objects) {
        if (componentType instanceof RegisteredComponentType) {
            final var registered = (RegisteredComponentType) componentType;
            return generic(registered.getPlugin(), msg, cause, objects);
        } else {
            return FormatUtil.rtExc(cause, msg, objects);
        }
    }

    public static RuntimeException genericOrRt(TargetType targetType, String msg, Throwable cause, Object... objects) {
        if (targetType instanceof RegisteredTargetType) {
            final var registered = (RegisteredTargetType) targetType;
            return generic(registered.getPlugin(), msg, cause, objects);
        } else {
            return FormatUtil.rtExc(cause, msg, objects);
        }
    }

    public static RuntimeException inEventListener(EventListener<?, ?, ?> listener, Object event, Object target, Throwable cause) {
        return genericOrRt(listener.getComponentType(), "Exception thrown in event listener [] while handling event []", cause, listener, event);
    }

    public static RuntimeException inComponentInit(ComponentType componentType, Throwable cause) {
        return genericOrRt(componentType, "Exception thrown while initialising component []", cause, componentType);
    }

    public static RuntimeException inComponentInterface(ComponentType componentType, TypeRef<?> interfaceType, Throwable cause) {
        return genericOrRt(componentType, "Exception thrown while applying interface [] for component type []", cause, interfaceType, componentType);
    }

    public void addPluginToStack(LoadedPlugin plugin) {
        this.stack.add(Objects.requireNonNull(plugin));
    }

    public void addComponentToStack(ComponentType componentType) {
        Objects.requireNonNull(componentType);
        if (componentType instanceof RegisteredComponentType) {
            final var registered = (RegisteredComponentType) componentType;
            this.stack.add(registered.getPlugin());
        }
    }

    public List<LoadedPlugin> getStack() {
        return Collections.unmodifiableList(stack);
    }

    public LoadedPlugin getSource() {
        return stack.get(0);
    }

}
