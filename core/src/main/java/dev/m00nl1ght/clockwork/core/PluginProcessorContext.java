package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Arguments;

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
        plugin.getClockworkCore().getState().require(ClockworkCore.State.PROCESSING);
        final var targetModule = targetClass.getModule();
        if (targetModule != plugin.getMainModule())
            throw new IllegalAccessException();
        ClockworkLoader.class.getModule().addReads(targetClass.getModule());
        return MethodHandles.privateLookupIn(targetClass, rootLookup);
    }

    public LoadedPlugin getPlugin() {
        return plugin;
    }

    @Override
    public String toString() {
        return plugin.toString();
    }

}
