package dev.m00nl1ght.clockwork.util;

import dev.m00nl1ght.clockwork.core.ComponentDefinition;
import dev.m00nl1ght.clockwork.core.PluginContainer;
import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.resolver.PluginLoadingProblem;

import java.util.List;

public class PluginLoadingException extends RuntimeException {

    protected PluginLoadingException(String msg) {
        super(msg);
    }

    protected PluginLoadingException(String msg, Exception cause) {
        super(msg, cause);
    }

    public static PluginLoadingException generic(String msg) {
        return new PluginLoadingException(msg);
    }

    public static PluginLoadingException generic(String msg, Exception cause) {
        return new PluginLoadingException(msg, cause);
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

    public static PluginLoadingException componentClassIllegal(String className, PluginContainer<?> plugin, String actPlugin, String module) {
        if (actPlugin == null) {
            return generic("Component class [" + className + "] for plugin [" + plugin.getId() + "] found in external module [" + module + "]");
        } else {
            return generic("Component class [" + className + "] for plugin [" + plugin.getId() + "] found in module [" + module + "] of plugin [" + actPlugin + "]");
        }
    }

    public static PluginLoadingException pluginMainModule(PluginDefinition definition, String pId) {
        if (pId == null) {
            return generic("Main module [" + definition.getMainModule() + "] defined for plugin [" + definition.getId() + "] not found");
        } else {
            return generic("Main module [" + definition.getMainModule() + "] defined for plugin [" + definition.getId() + "] is owned by another plugin [" + pId + "]");
        }
    }

}
