package dev.m00nl1ght.clockwork.loader;

import dev.m00nl1ght.clockwork.component.TargetType;
import dev.m00nl1ght.clockwork.core.ClockworkException;
import dev.m00nl1ght.clockwork.core.LoadedPlugin;
import dev.m00nl1ght.clockwork.core.RegisteredTargetType;
import dev.m00nl1ght.clockwork.descriptor.ComponentDescriptor;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;
import dev.m00nl1ght.clockwork.descriptor.TargetDescriptor;
import dev.m00nl1ght.clockwork.utils.logger.Logger;

import java.util.List;

public class PluginLoadingException extends ClockworkException {

    private PluginLoadingException(String message) {
        super(message);
    }

    private PluginLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ClockworkException generic(String message, Object... objects) {
        return new PluginLoadingException(Logger.formatMessage(message, objects));
    }

    public static ClockworkException generic(Throwable cause, String message, Object... objects) {
        return new PluginLoadingException(Logger.formatMessage(message, objects), cause);
    }

    public static ClockworkException fatalLoadingProblems(List<PluginLoadingProblem> problems) {
        return new PluginLoadingException("Fatal problems occured during dependency resolution");
    }

    public static ClockworkException resolvingModules(Exception exception, PluginDescriptor blame) {
        if (blame == null) {
            return new PluginLoadingException("An exception was thrown while resolving modules", exception);
        } else {
            return new PluginLoadingException(Logger.formatMessage(
                    "Failed to resolve modules for plugin []",
                    blame.getId()), exception).addToStack(blame.getId());
        }
    }

    public static ClockworkException componentMissingTarget(ComponentDescriptor component) {
        return new PluginLoadingException(Logger.formatMessage(
                "Could not find target [] for component []",
                component.getTargetId(), component.getId())).addToStack(component.getId());
    }

    public static ClockworkException targetMissingParent(TargetDescriptor target) {
        return new PluginLoadingException(Logger.formatMessage(
                "Could not find parent [] for target []",
                target.getParent(), target.getId())).addToStack(target.getId());
    }

    public static ClockworkException pluginClassIllegal(Class<?> clazz, LoadedPlugin plugin) {
        final var expectedName = plugin.getMainModule().getName() != null ? plugin.getMainModule().getName() : "UNNAMED";
        final var actualName = clazz.getModule().getName() != null ? clazz.getModule().getName() : "UNNAMED";
        return new PluginLoadingException(Logger.formatMessage(
                "Class [] for plugin [] was expected in its main module [], but was found in module []",
                clazz, plugin.getId(), expectedName, actualName)).addPluginToStack(plugin);
    }

    public static ClockworkException pluginClassNotFound(String className, PluginDescriptor plugin) {
        return new PluginLoadingException(Logger.formatMessage(
                "Class [] for plugin [] not found",
                className, plugin.getId())).addToStack(plugin.getId());
    }

    public static ClockworkException componentClassDuplicate(ComponentDescriptor component, String existing) {
        return new PluginLoadingException(Logger.formatMessage(
                "Component class [] defined for component type [] is already defined for component []",
                component.getComponentClass(), component.getId(), existing)).addToStack(component.getId());
    }

    public static ClockworkException componentIdDuplicate(ComponentDescriptor component, String existing) {
        return new PluginLoadingException(Logger.formatMessage(
                "Multiple component definitions with the same id [] are present",
                existing)).addToStack(component.getId());
    }

    public static ClockworkException targetClassDuplicate(TargetDescriptor target, String existing) {
        return new PluginLoadingException(Logger.formatMessage(
                "Target class [] defined for target type [] is already defined for target []",
                target.getTargetClass(), target.getId(), existing)).addToStack(target.getId());
    }

    public static ClockworkException targetIdDuplicate(TargetDescriptor target, String existing) {
        return new PluginLoadingException(Logger.formatMessage(
                "Multiple target definitions with the same id [] are present",
                existing)).addToStack(target.getId());
    }

    public static ClockworkException inProcessor(String processor, Throwable cause) {
        return new PluginLoadingException(Logger.formatMessage(
                "PluginProcessor [] threw an exception",
                processor), cause);
    }

    public static ClockworkException inProcessor(LoadedPlugin plugin, String processor, Throwable cause) {
        return new PluginLoadingException(Logger.formatMessage(
                "PluginProcessor [] threw an exception while processing plugin []",
                processor, plugin), cause).addPluginToStack(plugin);
    }

    public static ClockworkException invalidParentForTarget(TargetDescriptor target, RegisteredTargetType<?> parent) {
        return new PluginLoadingException(Logger.formatMessage(
                "Target [] cannot be set as parent for target [] (subclass mismatch)",
                parent.getId(), target.getId())).addToStack(target.getId());
    }

    public static ClockworkException illegalSubtarget(TargetType<?> target, TargetType<?> other) {
        return new PluginLoadingException(Logger.formatMessage(
                "Class [] extends class [], but respective target [] does not extend target []",
                other.getTargetClass().getSimpleName(), target.getTargetClass().getSimpleName(), other, target)).addTargetToStack(target);
    }

    public static ClockworkException invalidTargetClass(TargetDescriptor target, Class<?> targetClass) {
        return new PluginLoadingException(Logger.formatMessage(
                "Target class [] defined for target type [] does not implement ComponentTarget interface",
                targetClass.getSimpleName(), target.getId())).addToStack(target.getId());
    }

    public static ClockworkException invalidComponentClass(ComponentDescriptor component, Class<?> componentClass) {
        return new PluginLoadingException(Logger.formatMessage(
                "Component class [] defined for component type [] does not properly implement Component<T>",
                componentClass.getSimpleName(), component.getId())).addToStack(component.getId());
    }

    public static ClockworkException dependencyDuplicate(String of, DependencyDescriptor dependency, DependencyDescriptor existing) {
        return new PluginLoadingException(Logger.formatMessage(
                "Duplicate dependency defined for []: [] vs. []",
                of, dependency, existing)).addToStack(of);
    }

    public static ClockworkException pluginDuplicate(PluginDescriptor plugin, PluginDescriptor existing) {
        return new PluginLoadingException(Logger.formatMessage(
                "Multiple plugins with the same id [] are present",
                plugin.getId())).addToStack(plugin.getId());
    }

}
