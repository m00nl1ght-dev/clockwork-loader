package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

public class PluginProcessorContext {

    private final LoadedPlugin plugin;
    private final Lookup rootLookup;

    PluginProcessorContext(LoadedPlugin plugin, Lookup rootLookup) {
        this.plugin = Arguments.notNull(plugin, "plugin");
        this.rootLookup = Arguments.notNullAnd(rootLookup, Lookup::hasFullPrivilegeAccess, "rootLookup");
        plugin.getClockworkCore().getState().require(ClockworkCore.State.PROCESSING);
        if (rootLookup.lookupClass() != ClockworkLoader.class) throw new IllegalArgumentException();
    }

    public Lookup getReflectiveAccess(Class<?> targetClass) throws IllegalAccessException {
        Arguments.notNull(targetClass, "targetClass");
        plugin.getClockworkCore().getState().require(ClockworkCore.State.PROCESSING);
        final var targetModule = targetClass.getModule();
        if (targetModule != plugin.getMainModule()) throw new IllegalAccessException();
        ClockworkLoader.class.getModule().addReads(targetClass.getModule());
        return MethodHandles.privateLookupIn(targetClass, rootLookup);
    }

    public <C, T extends ComponentTarget> ComponentFactory<T, C>
    getComponentFactory(RegisteredComponentType<C, T> componentType) {
        Arguments.notNull(componentType, "componentType");
        plugin.getClockworkCore().getState().require(ClockworkCore.State.PROCESSING);
        this.checkPluginAccess(componentType.getPlugin());
        return componentType.getFactoryInternal();
    }

    public <C, T extends ComponentTarget> void
    setComponentFactory(RegisteredComponentType<C, T> componentType, ComponentFactory<T, C> factory) {
        Arguments.notNull(componentType, "componentType");
        Arguments.notNull(factory, "factory");
        plugin.getClockworkCore().getState().require(ClockworkCore.State.PROCESSING);
        this.checkPluginAccess(componentType.getPlugin());
        componentType.setFactoryInternal(factory);
    }

    private void checkPluginAccess(LoadedPlugin other) {
        if (other != plugin)
            throw FormatUtil.illArgExc("Context of [] can not access plugin []", plugin, other);
    }

    public LoadedPlugin getPlugin() {
        return plugin;
    }

    @Override
    public String toString() {
        return plugin.toString();
    }

}
