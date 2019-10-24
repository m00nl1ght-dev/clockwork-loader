package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.LogUtil;

import java.util.List;

public class PluginLoadingException extends RuntimeException {

    protected PluginLoadingException(String msg) {
        super(msg);
    }

    protected PluginLoadingException(String msg, Exception cause) {
        super(msg, cause);
    }

    public static PluginLoadingException generic(String msg, Object... objects) {
        return new PluginLoadingException(LogUtil.format(msg, "[]", objects));
    }

    public static PluginLoadingException generic(String msg, Exception cause, Object... objects) {
        return new PluginLoadingException(LogUtil.format(msg, "[]", objects), cause);
    }

    public static PluginLoadingException fatalLoadingProblems(List<PluginLoadingProblem> problems) {
        return generic("Fatal problems occured during dependency resolution");
    }

    public static PluginLoadingException inModuleFinder(Exception exception, PluginDefinition blame) {
        if (blame == null) {
            return generic("An exception was thrown while resolving modules", exception);
        } else {
            return generic("Failed to find modules for plugin [" + blame.getId() + "]", exception);
        }
    }

    public static PluginLoadingException componentMissingTarget(ComponentDefinition definition) {
        return generic("Could not find target [" + definition.getTargetId() + "] for component [" + definition.getId() + "]");
    }

    public static PluginLoadingException componentClassIllegal(String className, PluginContainer plugin, String actPlugin, String module) {
        if (actPlugin == null) {
            return generic("Component class [" + className + "] for plugin [" + plugin.getId() + "] found in external module [" + module + "]");
        } else {
            return generic("Component class [" + className + "] for plugin [" + plugin.getId() + "] found in module [" + module + "] of plugin [" + actPlugin + "]");
        }
    }

    public static PluginLoadingException componentClassNotFound(String className, PluginContainer plugin) {
        return generic("Class [" + className + "] defined for plugin [" + plugin.getId() + "] not found");
    }

    public static PluginLoadingException componentClassDuplicate(ComponentDefinition def, String existing) {
        return generic("Component class [" + def.getComponentClass() + "] defined for component type [" + def.getId() + "] is already defined for component [" + existing + "]");
    }

    public static PluginLoadingException targetClassDuplicate(ComponentTargetDefinition def, String existing) {
        return generic("Target class [" + def.getTargetClass() + "] defined for target type [" + def.getId() + "] is already defined for target [" + existing + "]");
    }

    public static PluginLoadingException loaderForUnknownModule(String moduleName) {
        return generic("Cannot create classloader for unknown module [" + moduleName + "]");
    }

    public static PluginLoadingException pluginMainModuleNotFound(PluginDefinition definition) {
        return generic("Main module [" + definition.getMainModule() + "] defined for plugin [" + definition.getId() + "] not found");
    }

    public static PluginLoadingException pluginMainModuleIllegal(PluginDefinition definition, String pId) {
        if (pId == null) {
            return generic("Main module [" + definition.getMainModule() + "] defined for plugin [" + definition.getId() + "] is in a different layer");
        } else {
            return generic("Main module [" + definition.getMainModule() + "] defined for plugin [" + definition.getId() + "] is owned by another plugin [" + pId + "]");
        }
    }

}
