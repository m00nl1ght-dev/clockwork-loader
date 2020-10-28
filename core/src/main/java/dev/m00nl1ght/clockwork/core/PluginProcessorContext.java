package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Objects;

public class PluginProcessorContext {

    private final LoadedPlugin plugin;
    private final Lookup rootLookup;

    PluginProcessorContext(LoadedPlugin plugin, Lookup rootLookup) {
        this.plugin = Objects.requireNonNull(plugin);
        this.rootLookup = Objects.requireNonNull(rootLookup);
        if (!rootLookup.hasFullPrivilegeAccess()) throw new IllegalArgumentException();
        plugin.getClockworkCore().getState().require(ClockworkCore.State.POPULATED);
        if (rootLookup.lookupClass() != ClockworkLoader.class) throw new IllegalArgumentException();
    }

    public Lookup getReflectiveAccess(Class<?> targetClass) throws IllegalAccessException {
        Objects.requireNonNull(targetClass);
        plugin.getClockworkCore().getState().require(ClockworkCore.State.POPULATED);
        final var targetModule = targetClass.getModule();
        if (targetModule != plugin.getMainModule()) throw new IllegalAccessException();
        ClockworkLoader.class.getModule().addReads(targetClass.getModule());
        return MethodHandles.privateLookupIn(targetClass, rootLookup);
    }

    public <C, T extends ComponentTarget> ComponentFactory<T, C>
    getComponentFactory(RegisteredComponentType<C, T> componentType) {
        Objects.requireNonNull(componentType);
        plugin.getClockworkCore().getState().require(ClockworkCore.State.POPULATED);
        this.checkPluginAccess(componentType.getPlugin());
        return componentType.getFactoryInternal();
    }

    public <C, T extends ComponentTarget> void
    setComponentFactory(RegisteredComponentType<C, T> componentType, ComponentFactory<T, C> factory) {
        Objects.requireNonNull(componentType);
        Objects.requireNonNull(factory);
        plugin.getClockworkCore().getState().require(ClockworkCore.State.POPULATED);
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
