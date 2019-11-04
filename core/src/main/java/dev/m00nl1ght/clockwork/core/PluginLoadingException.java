package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.LogUtil;

import java.lang.module.ModuleDescriptor;
import java.util.List;

public class PluginLoadingException extends RuntimeException {

    protected PluginLoadingException(String msg) {
        super(msg);
    }

    protected PluginLoadingException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public static PluginLoadingException generic(String msg, Object... objects) {
        return new PluginLoadingException(LogUtil.format(msg, "[]", objects));
    }

    public static PluginLoadingException generic(String msg, Throwable cause, Object... objects) {
        return new PluginLoadingException(LogUtil.format(msg, "[]", objects), cause);
    }

    public static PluginLoadingException fatalLoadingProblems(List<PluginLoadingProblem> problems) {
        return generic("Fatal problems occured during dependency resolution");
    }

    public static PluginLoadingException inModuleFinder(Exception exception, PluginDefinition blame) {
        if (blame == null) {
            return generic("An exception was thrown while resolving modules", exception);
        } else {
            return generic("Failed to find modules for plugin []", exception, blame.getId());
        }
    }

    public static PluginLoadingException componentMissingTarget(ComponentDefinition definition) {
        return generic("Could not find target [] for component []", definition.getTargetId(), definition.getId());
    }

    public static PluginLoadingException componentClassIllegal(String className, PluginContainer plugin, String actPlugin, ModuleDescriptor module) {
        if (module == null || actPlugin == null) {
            return generic("Component class [] for plugin [] found in external module []", className, plugin.getId(), module == null ? "UNNAMED" : module.name());
        } else {
            return generic("Component class [] for plugin [] found in module [] of plugin []", className, plugin.getId(), module.name(), actPlugin);
        }
    }

    public static PluginLoadingException componentClassNotFound(String className, PluginContainer plugin) {
        return generic("Class [] defined for plugin [] not found", className, plugin.getId());
    }

    public static PluginLoadingException componentClassDuplicate(ComponentDefinition def, String existing) {
        return generic("Component class [] defined for component type [] is already defined for component []", def.getComponentClass(), def.getId(), existing);
    }

    public static PluginLoadingException targetClassDuplicate(ComponentTargetDefinition def, String existing) {
        return generic("Target class [] defined for target type [] is already defined for target []", def.getTargetClass(), def.getId(), existing);
    }

    public static PluginLoadingException loaderForUnknownModule(String moduleName) {
        return generic("Cannot create classloader for unknown module []", moduleName);
    }

    public static PluginLoadingException pluginMainModuleNotFound(PluginDefinition definition) {
        return generic("Main module [] defined for plugin [] not found", definition.getMainModule(), definition.getId());
    }

    public static PluginLoadingException pluginMainModuleIllegal(PluginDefinition definition, String pId) {
        if (pId == null) {
            return generic("Main module [] defined for plugin [] is in a different layer", definition.getMainModule(), definition.getId());
        } else {
            return generic("Main module [] defined for plugin [] is owned by another plugin []", definition.getMainModule(), definition.getId(), pId);
        }
    }

    public static PluginLoadingException invalidId(PluginDefinition plugin, String id) {
        return generic("Plugin [] defines id [] containing invalid character", plugin.getId(), id);
    }

    public static PluginLoadingException subIdMismatch(PluginDefinition plugin, String id) {
        return generic("Component id [] does not match the plugin [] that defines it", id, plugin.getId());
    }

    public static PluginLoadingException missingProcessor(String text, String id, String name) {
        return generic("PluginProcessor [] defined for " + text + " [] is missing", name, id);
    }

    public static PluginLoadingException inProcessor(String text, String id, String name, Throwable cause) {
        return generic("PluginProcessor [] threw an exception while processing " + text + " []", cause, name, id);
    }

    public static PluginLoadingException noEventTypeFactoryFound(Class<?> eventClass) {
        return generic("No event type factory found for event class []", eventClass.getName());
    }

}
