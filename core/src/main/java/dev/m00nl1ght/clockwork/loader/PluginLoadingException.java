package dev.m00nl1ght.clockwork.loader;

import dev.m00nl1ght.clockwork.core.LoadedPlugin;
import dev.m00nl1ght.clockwork.core.RegisteredTargetType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.descriptor.ComponentDescriptor;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;
import dev.m00nl1ght.clockwork.descriptor.TargetDescriptor;
import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.util.List;

public class PluginLoadingException extends RuntimeException {

    private PluginLoadingException(String msg) {
        super(msg);
    }

    private PluginLoadingException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public static PluginLoadingException generic(String msg, Object... objects) {
        return new PluginLoadingException(FormatUtil.format(msg, objects));
    }

    public static PluginLoadingException generic(String msg, Throwable cause, Object... objects) {
        return new PluginLoadingException(FormatUtil.format(msg, objects), cause);
    }

    public static PluginLoadingException fatalLoadingProblems(List<PluginLoadingProblem> problems) {
        return generic("Fatal problems occured during dependency resolution");
    }

    public static PluginLoadingException resolvingModules(Exception exception, PluginDescriptor blame) {
        if (blame == null) {
            return generic("An exception was thrown while resolving modules", exception);
        } else {
            return generic("Failed to resolve modules for plugin []", exception, blame.getId());
        }
    }

    public static PluginLoadingException componentMissingTarget(ComponentDescriptor component) {
        return generic("Could not find target [] for component []", component.getTargetId(), component.getId());
    }

    public static PluginLoadingException targetMissingParent(TargetDescriptor target) {
        return generic("Could not find parent [] for target []", target.getParent(), target.getId());
    }

    public static PluginLoadingException pluginClassIllegal(Class<?> clazz, LoadedPlugin plugin) {
        final var expectedName = plugin.getMainModule().getName() != null ? plugin.getMainModule().getName() : "UNNAMED";
        final var actualName = clazz.getModule().getName() != null ? clazz.getModule().getName() : "UNNAMED";
        return generic("Class [] for plugin [] was expected in its main module [], but was found in module []", clazz, plugin.getId(), expectedName, actualName);
    }

    public static PluginLoadingException pluginClassNotFound(String className, PluginDescriptor plugin) {
        return generic("Class [] for plugin [] not found", className, plugin.getId());
    }

    public static PluginLoadingException componentClassDuplicate(ComponentDescriptor component, String existing) {
        return generic("Component class [] defined for component type [] is already defined for component []", component.getComponentClass(), component.getId(), existing);
    }

    public static PluginLoadingException componentIdDuplicate(ComponentDescriptor component, String existing) {
        return generic("Multiple component definitions with the same id [] are present", existing);
    }

    public static PluginLoadingException targetClassDuplicate(TargetDescriptor target, String existing) {
        return generic("Target class [] defined for target type [] is already defined for target []", target.getTargetClass(), target.getId(), existing);
    }

    public static PluginLoadingException targetIdDuplicate(TargetDescriptor target, String existing) {
        return generic("Multiple target definitions with the same id [] are present", existing);
    }

    public static PluginLoadingException missingReader(String name) {
        return generic("No PluginReader with name [] is defined", name);
    }

    public static PluginLoadingException missingFinder(String name) {
        return generic("No PluginFinder with name [] is defined", name);
    }

    public static PluginLoadingException missingVerifier(String name) {
        return generic("No PluginVerifier with name [] is defined", name);
    }

    public static PluginLoadingException missingProcessor(String plugin, String name) {
        return generic("PluginProcessor [] needed by plugin [] is missing", name, plugin);
    }

    public static PluginLoadingException inProcessor(String processor, Throwable cause) {
        return generic("PluginProcessor [] threw an exception", cause, processor);
    }

    public static PluginLoadingException inProcessor(LoadedPlugin plugin, String processor, Throwable cause) {
        return generic("PluginProcessor [] threw an exception while processing plugin []", cause, processor, plugin);
    }

    public static PluginLoadingException invalidParentForTarget(TargetDescriptor target, RegisteredTargetType<?> parent) {
        return generic("Target [] cannot be set as parent for target [] (subclass mismatch)", parent.getId(), target.getId());
    }

    public static PluginLoadingException illegalSubtarget(TargetType<?> target, TargetType<?> other) {
        return generic("Class [] extends class [], but respective target [] does not extend target []", other.getTargetClass().getSimpleName(), target.getTargetClass().getSimpleName(), other, target);
    }

    public static PluginLoadingException invalidTargetClass(TargetDescriptor target, Class<?> targetClass) {
        return generic("Target class [] defined for target type [] does not implement ComponentTarget interface", targetClass.getSimpleName(), target.getId());
    }

    public static PluginLoadingException invalidComponentClass(ComponentDescriptor component, Class<?> componentClass) {
        return generic("Component class [] defined for component type [] does not properly implement Component<T>", componentClass.getSimpleName(), component.getId());
    }

    public static PluginLoadingException dependencyDuplicate(String of, DependencyDescriptor dependency, DependencyDescriptor existing) {
        return generic("Duplicate dependency defined for []: [] vs. []", of, dependency, existing);
    }

    public static PluginLoadingException pluginDuplicate(PluginDescriptor plugin, PluginDescriptor existing) {
        return generic("Multiple plugins with the same id [] are present", plugin.getId());
    }

}
