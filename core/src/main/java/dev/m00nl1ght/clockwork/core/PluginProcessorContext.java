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
        this.rootLookup = Arguments.notNull(rootLookup, "rootLookup");
        plugin.getClockworkCore().getState().require(ClockworkCore.State.PROCESSING);
        if (!rootLookup.hasFullPrivilegeAccess()) throw new IllegalArgumentException();
        if (rootLookup.lookupClass() != ClockworkLoader.class) throw new IllegalArgumentException();
    }

    public Lookup getReflectiveAccess(Class<?> targetClass) throws IllegalAccessException {
        plugin.getClockworkCore().getState().require(ClockworkCore.State.PROCESSING);
        final var targetModule = targetClass.getModule();
        if (targetModule != plugin.getMainModule())
            throw new IllegalAccessException();
        ClockworkLoader.class.getModule().addReads(targetClass.getModule());
        return MethodHandles.privateLookupIn(targetClass, rootLookup);
    }

    public <C, T extends ComponentTarget> void
    setComponentFactory(ComponentType<C, T> componentType, ComponentFactory<T, C> factory) {
        plugin.getClockworkCore().getState().require(ClockworkCore.State.PROCESSING);
        this.checkPluginAccess(componentType);
        componentType.setFactoryInternal(factory);
    }

    public <C, T extends ComponentTarget> ComponentFactory<T, C>
    getComponentFactory(ComponentType<C, T> componentType) {
        plugin.getClockworkCore().getState().require(ClockworkCore.State.PROCESSING);
        this.checkPluginAccess(componentType);
        return componentType.getFactoryInternal();
    }

    public LoadedPlugin getPlugin() {
        return plugin;
    }

    @Override
    public String toString() {
        return plugin.toString();
    }

    private void checkPluginAccess(ComponentType<?, ?> componentType) {
        if (componentType.getPlugin() != plugin)
            throw FormatUtil.illArgExc("Context of [] can not access component []", plugin, componentType);
    }

}
