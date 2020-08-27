package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.descriptor.ComponentDescriptor;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;
import dev.m00nl1ght.clockwork.descriptor.TargetDescriptor;
import dev.m00nl1ght.clockwork.locator.PluginLocator;
import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.lang.module.ModuleDescriptor;
import java.nio.file.Path;
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

    public static PluginLoadingException coreTargetMissing(String id) {
        return generic("Internal target [] is missing", id);
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

    public static PluginLoadingException componentClassIllegal(String className, PluginDescriptor plugin, String actPlugin, ModuleDescriptor module) {
        if (module == null || actPlugin == null) {
            return generic("Component class [] for plugin [] found in external module []", className, plugin.getId(), module == null ? "UNNAMED" : module.name());
        } else {
            return generic("Component class [] for plugin [] found in module [] of plugin []", className, plugin.getId(), module.name(), actPlugin);
        }
    }

    public static PluginLoadingException componentClassNotFound(String className, PluginDescriptor plugin) {
        return generic("Class [] defined for plugin [] not found", className, plugin.getId());
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

    public static PluginLoadingException pluginMainModuleNotFound(PluginDescriptor plugin, String moduleName) {
        return generic("Main module [] defined for plugin [] not found", moduleName, plugin.getId());
    }

    public static PluginLoadingException pluginMainModuleIllegal(PluginDescriptor plugin, String moduleName, String pId) {
        if (pId == null) {
            return generic("Main module [] defined for plugin [] is in a different layer", moduleName, plugin.getId());
        } else {
            return generic("Main module [] defined for plugin [] is owned by another plugin []", moduleName, plugin.getId(), pId);
        }
    }

    public static PluginLoadingException invalidId(String id) {
        return generic("Id [] is invalid", id);
    }

    public static PluginLoadingException subIdMismatch(PluginDescriptor plugin, String id) {
        return generic("Component id [] does not match the plugin [] that defines it", id, plugin.getId());
    }

    public static PluginLoadingException missingProcessor(String id, String name) {
        return generic("PluginProcessor [] needed by plugin [] is missing", name, id);
    }

    public static PluginLoadingException inProcessor(String text, String id, String name, Throwable cause) {
        return generic("PluginProcessor [] threw an exception while processing " + text + " []", cause, name, id);
    }

    public static PluginLoadingException invalidParentForTarget(TargetDescriptor target, TargetType<?> parent) {
        return generic("Target [] cannot be set as parent for target [] (subclass mismatch)", parent.getId(), target.getId());
    }

    public static PluginLoadingException illegalTargetSubclass(TargetDescriptor target, Class<?> targetClass, TargetType<?> sub) {
        return generic("[] is a subclass of class [], but target [] is not a parent of target []", sub.getTargetClass().getSimpleName(), targetClass.getSimpleName(), target.getId(), sub.getId());
    }

    public static PluginLoadingException invalidTargetClass(TargetDescriptor target, Class<?> targetClass) {
        return generic("Target class [] defined for target type [] does not implement ComponentTarget interface", targetClass.getSimpleName(), target.getId());
    }

    public static PluginLoadingException dependencyDuplicate(String of, DependencyDescriptor dependency, DependencyDescriptor existing) {
        return generic("Duplicate dependency defined for []: [] vs. []", of, dependency, existing);
    }

    public static PluginLoadingException pluginDuplicate(PluginDescriptor plugin, PluginDescriptor existing) {
        return generic("Multiple plugins with the same id [] are present", plugin.getId());
    }

    public static PluginLoadingException multipleModulesFound(PluginLocator locator, Path path) {
        return generic("[] found multiple java modules in path []", locator.getName(), path);
    }

}
